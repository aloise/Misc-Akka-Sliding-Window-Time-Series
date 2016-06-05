package name.aloise.data

import scala.collection.{immutable, mutable}
import scala.collection.JavaConversions._

/**
  * User: aloise
  * Date: 05.06.16
  * Time: 16:20
  */

/**
  * It would always add a new element to a group and possibly remove previously pushed elements
 *
  * @param belongsToGroup ( NEWEST ELEMENT, OLDEST ELEMENT ) => it would remove elements from tail until the function returns false
  * @tparam A element type
  */
class SlidingSeq[A](belongsToGroup: (A,A) => Boolean ) extends Iterable[A] {

  protected[data] var buffer = new java.util.LinkedList[A]()

  /**
    * It would add a new element from the queue and remove tail elements until belongsToAGroup would return false
    *
    * @param elem an element to push
    */
  def push(elem:A) = {

    elementPushed(elem)
    buffer.addFirst( elem )

    while( ( buffer.size() > 1 ) && !belongsToGroup( elem, buffer.peekLast() ) ){
      elementRemoved(buffer.peekLast())
      buffer.removeLast()
    }
  }

  override def iterator: Iterator[A] = buffer.iterator()

  /** Internal method to be called on every new element - before it's appended to a buffer */
  protected def elementPushed( elem:A ) {}

  /** Internal method to be called on every element removal - before it's deleted from the buffer */
  protected def elementRemoved( elem:A ) {}


}
