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

  /**
    *
    * @param source Input Source
    * @param sink Output Sink
    * @param windowDuration Event window duration
    * @param outputBuilder Output value builder from the list of parameters : Input, Index in window, Sum in window, Min in window, Max in window
    * @tparam A Input Value type
    * @tparam R Resulting Output
    * @return Result future
    */
  def run[A <: InputValue, R](
    source:Source[A,_],
    sink:Sink[OutputValue, Future[R]],
    windowDuration:Duration,
    outputBuilder: ( A, Int, BigDecimal, BigDecimal, BigDecimal ) => OutputValue =
      ( i:A, num:Int, sum:BigDecimal, min:BigDecimal, max:BigDecimal ) => new OutputValue( i, num, sum, min, max )
  ):Future[R] = {
    source.
      via( new SlidingTimeSeriesProcessor[A]( windowDuration, outputBuilder ) ).
      runWith(sink)
  }
}
