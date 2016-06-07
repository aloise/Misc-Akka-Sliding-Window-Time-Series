package name.aloise.data

import scala.math.ScalaNumber

/**
  * User: aloise
  * Date: 05.06.16
  * Time: 23:26
*/


/**
  * Provides a sliding sequence implementation with stats ( min, max, sum, index in window ).
  * Min, Max, Sum, Index is computed in O(1)
  *
  * @param belongsToGroup ( NEWEST ELEMENT, OLDEST ELEMENT ) => it would remove elements from tail until the function returns false
  * @tparam A element type
*/

class SlidingSeqMetrics[A](
  belongsToGroup: ( A, A ) => Boolean
)(
  implicit ordering : Ordering[A],
  sumMapper: A => BigDecimal
) extends SlidingSeq[A]( belongsToGroup ) {

  /**
    * It is used to implement min / max heap in O(1)
  */
  val minStack = new java.util.LinkedList[A]()
  val maxStack = new java.util.LinkedList[A]()

  var windowSum = BigDecimal(0)

  def min = minStack.getFirst
  def max = maxStack.getFirst
  def num = buffer.size()
  def sum = windowSum

  override def afterPush( elem:A ):Unit = {

    while( !minStack.isEmpty && ordering.gt( minStack.getLast , elem)) {
      minStack.removeLast()
    }
    minStack.addLast(elem)


    while( !maxStack.isEmpty && ordering.lt( maxStack.getLast, elem) ){
      maxStack.removeLast()
    }
    maxStack.addLast(elem)

    windowSum = sumMapper(elem) + windowSum

  }

  override def afterRemove( elem:A ):Unit = {

    if( !minStack.isEmpty && ordering.equiv( elem, minStack.getFirst ) ){
        minStack.removeFirst()
    }

    if( !maxStack.isEmpty && ordering.equiv( elem, maxStack.getFirst ) ){
        maxStack.removeFirst()
    }

    windowSum = windowSum - sumMapper(elem)
  }


}
