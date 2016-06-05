package name.aloise.models

/**
  * User: aloise
  * Date: 04.06.16
  * Time: 16:52
  */
class InputValue( val timestamp:Int, val value:BigDecimal ) {
  override def toString:String = "InputValue("+timestamp+","+value + ")"
}
