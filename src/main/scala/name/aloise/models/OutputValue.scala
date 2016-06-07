package name.aloise.models

/**
  * User: aloise
  * Date: 04.06.16
  * Time: 16:52
  */
class OutputValue( input: InputValue, numberOfObservations:Int, rollingSum:BigDecimal, minValue:BigDecimal, maxValue:BigDecimal ) {

  override def toString:String = "OutputValue(" + input + "," + numberOfObservations + "," + rollingSum + "," + minValue + "," + maxValue + ")"

  def exportToString( fieldSeparator:String ):String =
    Seq( input.timestamp, input.value, numberOfObservations, rollingSum, minValue, maxValue ).map(_.toString).mkString(fieldSeparator)


}
