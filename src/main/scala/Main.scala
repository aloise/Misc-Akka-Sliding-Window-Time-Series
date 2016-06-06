import java.io.File
import java.net.URL
import java.nio.file.{Path, Paths}

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, Framing, Sink, Source}
import akka.util.ByteString
import name.aloise.TimeSeriesStreamProcessor
import name.aloise.models.{InputValue, OutputValue}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Random, Success, Try}

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


    val inputPath = Paths.get( "/home/aloise/work/www/aloise/challenge-fyber/docs/data_scala.txt" )

    val inputFile =
      FileIO.
        fromPath( inputPath ).
        via( Framing.delimiter( ByteString("\n"), 64, allowTruncation = true ) ).
        map( InputValue.parse ).
        collect{ case Success( input ) => input }

    val sink = Sink.foreach[OutputValue]( println )

    val processor = new TimeSeriesStreamProcessor

    val completedFuture =
      processor.
        run( inputFile, sink, 60.seconds ).
        map( _ => actorSystem.terminate() )


    Await.result( completedFuture, Duration.Inf )


  }

}
