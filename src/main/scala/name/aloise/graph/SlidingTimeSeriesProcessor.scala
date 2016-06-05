package name.aloise.graph

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import name.aloise.models._

import scala.collection.immutable
import scala.concurrent.duration.Duration

/**
  * User: aloise
  * Date: 04.06.16
  * Time: 17:01
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

      // contains elements in reverse order. First one is the last one
      protected var slidingWindow = List.empty[A]

      protected def buildOutput( input:A ):OutputValue = {
        val num = slidingWindow.length
        val sum = slidingWindow.map(_.value).sum
        val min = slidingWindow.map(_.value).min
        val max = slidingWindow.map(_.value).max

        outputBuilder( input, num, sum, min, max )
      }

      protected def filterWindow( inputElements:List[A] ) = {
        if( inputElements.nonEmpty ){

          val headTime = inputElements.head.timestamp

          inputElements.takeWhile( headTime - _.timestamp <= windowDuration.toSeconds )

        } else {
          inputElements
        }


      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {

          val elem = grab( in )

          /**
            * timestamp should increase over time
            * require it or skip
            */
          if( slidingWindow.nonEmpty ){
            require( elem.timestamp >= slidingWindow.head.timestamp )
          }

          slidingWindow = filterWindow( elem :: slidingWindow )

          push( out, buildOutput( elem ) )

        }

      })


      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })

    }

}
