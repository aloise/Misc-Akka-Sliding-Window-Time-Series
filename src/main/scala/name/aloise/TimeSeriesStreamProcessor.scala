package name.aloise

import java.util.concurrent.CompletionStage

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import name.aloise.graph.SlidingTimeSeriesProcessor
import name.aloise.models.{InputValue, OutputValue}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * User: aloise
  * Date: 04.06.16
  * Time: 16:51
  */
class TimeSeriesStreamProcessor( implicit actorSystem:ActorSystem, materializer:ActorMaterializer ) {

  def run( source:Source[InputValue,NotUsed], sink:Sink[OutputValue, Future[Done]], windowDuration:Duration ):Future[Done] = {
    source.
      via( new SlidingTimeSeriesProcessor(windowDuration) ).
      runWith(sink)
  }
}
