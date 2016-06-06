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
class SlidingSeq[A](belongsToGroup: (A,A) => Boolean ) extends SlidingSeqLike[A] with SlidingSeqAfterPush[A] with SlidingSeqBeforeRemove[A] {

  protected val buffer = new java.util.LinkedList[A]()

  /**
    * It would add a new element from the queue and remove tail elements until belongsToAGroup would return false
    *
    * @param elem an element to push
    */
  def push(elem:A) = {
    buffer.addFirst( elem )
    afterPush(elem)

    while( ( buffer.size() > 1 ) && !belongsToGroup( elem, buffer.peekLast() ) ){
      val elemToDelete = buffer.peekLast()
      buffer.removeLast()
      afterRemove(elemToDelete)
    }
  }

  override def last:A = buffer.getLast

  def length:Int = buffer.size

  override def iterator: Iterator[A] = buffer.iterator()


}
