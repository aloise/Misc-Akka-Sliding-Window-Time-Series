import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import name.aloise.TimeSeriesStreamProcessor
import name.aloise.models.{InputValue, OutputValue}
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util._

/**
  * User: aloise
  * Date: 05.06.16
  * Time: 12:12
  */
class ProcessingSpec extends FlatSpec with BeforeAndAfterAll with ShouldMatchers {

  implicit val actorSystem = ActorSystem("test")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  val outputData = """1355270609 1.80215 1 1.80215 1.80215 1.80215
                    |1355270621 1.80185 2 3.604 1.80185 1.80215
                    |1355270646 1.80195 3 5.40595 1.80185 1.80215
                    |1355270702 1.80225 2 3.6042 1.80195 1.80225
                    |1355270702 1.80215 3 5.40635 1.80195 1.80225
                    |1355270829 1.80235 1 1.80235 1.80235 1.80235
                    |1355270854 1.80205 2 3.6044 1.80205 1.80235
                    |1355270868 1.80225 3 5.40665 1.80205 1.80235
                    |1355271000 1.80245 1 1.80245 1.80245 1.80245
                    |1355271023 1.80285 2 3.6053 1.80245 1.80285
                    |1355271024 1.80275 3 5.40805 1.80245 1.80285
                    |1355271026 1.80285 4 7.2109 1.80245 1.80285
                    |1355271027 1.80265 5 9.01355 1.80245 1.80285
                    |1355271056 1.80275 6 10.8163 1.80245 1.80285
                    |1355271428 1.80265 1 1.80265 1.80265 1.80265
                    |1355271466 1.80275 2 3.6054 1.80265 1.80275
                    |1355271471 1.80295 3 5.40835 1.80265 1.80295
                    |1355271507 1.80265 3 5.40835 1.80265 1.80295
                    |1355271562 1.80275 2 3.6054 1.80265 1.80275
                    |1355271588 1.80295 2 3.6057 1.80275 1.80295""".trim.stripMargin

  val inputData = """1355270609	1.80215
                    |1355270621	1.80185
                    |1355270646	1.80195
                    |1355270702	1.80225
                    |1355270702	1.80215
                    |1355270829	1.80235
                    |1355270854	1.80205
                    |1355270868	1.80225
                    |1355271000	1.80245
                    |1355271023	1.80285
                    |1355271024	1.80275
                    |1355271026	1.80285
                    |1355271027	1.80265
                    |1355271056	1.80275
                    |1355271428	1.80265
                    |1355271466	1.80275
                    |1355271471	1.80295
                    |1355271507	1.80265
                    |1355271562	1.80275
                    |1355271588	1.80295""".trim.stripMargin

  case class SampleInputValue( override val timestamp:Int, override val value:BigDecimal )
    extends InputValue( timestamp, value )

  case class SampleOutputValue( input: InputValue, numberOfObservations:Int, rollingSum:BigDecimal, minValue:BigDecimal, maxValue:BigDecimal ) extends
    OutputValue( input, numberOfObservations, rollingSum, minValue, maxValue )


  val inputSeq = inputData.split('\n').map( InputValue.parse ).collect{ case Success( item ) => SampleInputValue( item.timestamp, item.value ) }.toList
  val outputSeq = outputData.split('\n').map{ item =>
    val Array( timestamp, value, num, sum, min, max ) = item.split("\\s+",6)
    sampleOutputBuilder( SampleInputValue( timestamp.toInt, BigDecimal(value)), num.toInt, BigDecimal(sum), BigDecimal(min), BigDecimal(max) )
  }.toList

  def sampleOutputBuilder( i:InputValue, num:Int, sum:BigDecimal, min:BigDecimal, max:BigDecimal ):SampleOutputValue = {
    SampleOutputValue( i, num, sum, min, max )
  }



  "A sliding window processor" should "return a correct empty on empty output" in {
    val processor = new TimeSeriesStreamProcessor()

    val result = Await.result( processor.run( Source.empty[InputValue], Sink.seq[OutputValue], 60.seconds ), 60.seconds )

    result shouldBe Nil
  }

  it should "process a single item" in {
    val processor = new TimeSeriesStreamProcessor()

    val futureResult = processor.run( Source.single( inputSeq.head), Sink.seq[OutputValue], 60.seconds, sampleOutputBuilder )

    val result = Await.result( futureResult, 60.seconds )

    result should not be empty
    result.head shouldBe outputSeq.head

  }

  it should "process the list of items" in {
    val processor = new TimeSeriesStreamProcessor()

    val futureResult = processor.run( Source( inputSeq ), Sink.seq[OutputValue], 60.seconds, sampleOutputBuilder )

    val result = Await.result( futureResult, 60.seconds )

    result should not be empty
    result shouldBe outputSeq

  }



  override def afterAll() = {

    actorSystem.terminate()

  }



}
