import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import name.aloise.TimeSeriesStreamProcessor
import name.aloise.models.{InputValue, OutputValue}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

/**
  * User: aloise
  * Date: 03.06.16
  * Time: 10:47
  */
object Main {

  implicit val actorSystem = ActorSystem("timeseries")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  def main(args: Array[String]) {


    val initialTimestamp = System.currentTimeMillis()/1000

    val source = Source( 1 to 100 ).map( i => new InputValue( initialTimestamp.toInt + i, BigDecimal( Random.nextInt(100) )/100 ) )

    val sink = Sink.foreach[OutputValue]( println )

    val processor = new TimeSeriesStreamProcessor

    val completedFuture =
      processor.
        run( source, sink, 5.seconds ).
        map( _ => actorSystem.terminate() )


    Await.result( completedFuture, Duration.Inf )


  }

}
