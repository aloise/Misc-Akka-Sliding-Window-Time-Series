import java.io.File
import java.net.URL
import java.nio.file.{NoSuchFileException, Path, Paths}

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, Framing, Sink, Source}
import akka.util.ByteString
import name.aloise.TimeSeriesStreamProcessor
import name.aloise.models.{InputValue, OutputValue}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util._

/**
  * User: aloise
  * Date: 03.06.16
  * Time: 10:47
  */
object Main {

  def main(args: Array[String]) {

    parseInputArgs(args) match {
      case Success( ( inputPath, outputPath, windowLength ) ) =>

          val completedFuture = processFiles( inputPath, outputPath, windowLength )

          val ioResult = Await.result( completedFuture, Duration.Inf )

          ioResult match {
            case IOResult( count, Success( _ ) ) =>
              println("Completed Successfully : " + count + " Bytes written")

            case IOResult( count, Failure( ex:NoSuchFileException )) =>
              println( "File Not Found : " + ex.getFile )
              System.exit(2)

            case IOResult( count, Failure( ex )) =>
              println("Failure during execution : " + ex.getMessage )
              System.exit(3)
          }

      case Failure( ex ) =>
        println( "Error : " + ex.getMessage )
        System.exit(1)


    }
  }

  /**
    *
    * @param args command line input arguments
    * @param defaultWindowLength Default sliding window length in seconds
    * @return A try with tuple that consists of input path, output path and sliding window duration
    */
  def parseInputArgs( args:Array[String], defaultWindowLength:Duration = 60.seconds ):Try[(Path,Path,Duration)] = {

    if( args.length < 2 ){
      Failure( new Exception( "Usage: java -jar " + getJarName + " INPUT_FILE OUTPUT_FILE [WINDOW_LENGTH_IN_SECONDS]" ) )
    } else {

      val inputFilename = args(0)
      val inputPath = Paths.get(inputFilename)

      val outputFilename = args(1)
      val outputPath = Paths.get(outputFilename)

      val windowLengthTry: Try[Duration] =
        if (args.length > 2) {
          Try( args(2).toInt ) match {
            case Success( windowSeconds ) if windowSeconds <= 0 =>
              Failure( new Exception("Window length should be passed as a positive number") )
            case Success( windowSeconds) =>
              Success( windowSeconds.seconds )
            case _ =>
              Failure( new Exception("Please provide a correct window length in seconds") )
          }
        } else Success( defaultWindowLength )


      for {
        wl <- windowLengthTry
      } yield ( inputPath, outputPath, wl )

    }
  }

  def processFiles( inputFile:Path, outputFile:Path, duration:Duration ) = {

    implicit val actorSystem = ActorSystem("timeseries")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = actorSystem.dispatcher

    // Builds a source from input file

    val source =
      FileIO.
        fromPath( inputFile ).
        via( Framing.delimiter( ByteString("\n"), 64, allowTruncation = true ) ).
        map( InputValue.parse ).
        collect{ case Success( input ) => input }

    // Builds a sink from output file
    val sink = FileIO.toPath( outputFile ).contramap[OutputValue] { o =>
      ByteString( o.exportToString( "\t" ) + "\n" )
    }

    val processor = new TimeSeriesStreamProcessor

    val completedFuture =
      processor.
        run( source, sink, duration )

    // shutdown the actor system on completion
    completedFuture.onComplete{ _ =>
      actorSystem.terminate()
    }

    completedFuture

  }

  def getJarName =
    new java.io.File(this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI.getPath).getName

}
