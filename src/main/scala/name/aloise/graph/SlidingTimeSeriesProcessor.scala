package name.aloise.graph

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import name.aloise.data.SlidingSeqMetrics
import name.aloise.models.{InputValue, OutputValue}

import scala.concurrent.duration.Duration

/**
  * User: aloise
  * Date: 06.06.16
  * Time: 22:49
  *
  * Akka stream Graph Stage
  * Performs a sliding window metrics calculation
  * It awaits new values in non-decreasing order. Invalid timestamps are ignored
  */
class SlidingTimeSeriesProcessor[ A <: InputValue](
    windowDuration:Duration,
    outputBuilder : (A, Int, BigDecimal, BigDecimal, BigDecimal) => OutputValue = ( i:A, num:Int, sum:BigDecimal, min:BigDecimal, max:BigDecimal ) => new OutputValue( i, num, sum, min, max )
) extends GraphStage[FlowShape[A,OutputValue]] {

  val in = Inlet[A]("SlidingTimeSeriesProcessor.in")
  val out = Outlet[OutputValue]("SlidingTimeSeriesProcessor.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(attr: Attributes): GraphStageLogic =

    new GraphStageLogic(shape) {

      // contains current sliding window.
      val slidingWindow = new SlidingSeqMetrics[A]( _.timestamp - _.timestamp <= windowDuration.toSeconds )(
        new Ordering[A] { override def compare(x: A, y: A): Int = x.value.compare( y.value ) },
        _.value
      )

      setHandler(in, new InHandler {
        override def onPush(): Unit = {

          val elem = grab( in )

          // timestamp must increase over time or be equal with the last one
          val skipElement = slidingWindow.nonEmpty && ( elem.timestamp < slidingWindow.head.timestamp )

          if ( skipElement ) {
            // skip the element - timestamp should increase over time
            // pull the next value
            pull(in)
          } else {

            slidingWindow.push(elem)

            // push the output
            push(out, outputBuilder(elem, slidingWindow.num, slidingWindow.sum, slidingWindow.min.value, slidingWindow.max.value))

          }

        }

      })


      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          // just pull a next value from upstream
          pull(in)
        }
      })

    }

}
