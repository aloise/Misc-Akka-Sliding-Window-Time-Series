package name.aloise.models

import akka.util.ByteString

import scala.util.Try

/**
  * User: aloise
  * Date: 04.06.16
  * Time: 16:52
  */
class InputValue( val timestamp:Int, val value:BigDecimal ) {
  override def toString:String = "InputValue("+timestamp+","+value + ")"
}

object InputValue {

  def parse( str:String ):Try[InputValue] = Try {
    val Array( timestamp, value ) = str.trim.split("\\s+", 2)

    new InputValue( timestamp.toInt, BigDecimal( value ) )
  }


  def parse( str:ByteString ):Try[InputValue] =
    parse( str.utf8String )

}