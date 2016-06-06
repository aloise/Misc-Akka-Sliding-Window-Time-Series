package name.aloise.data

/**
  * User: aloise
  * Date: 06.06.16
  * Time: 10:12
  */

trait SlidingSeqAfterPush[A] {
  /** Internal method to be called on every new element */
  def afterPush( elem:A ):Unit = {}
}

trait SlidingSeqBeforeRemove[A] {
  /** Internal method to be called on every element removal */
  def afterRemove( elem:A ):Unit = {}
}

trait SlidingSeqLike[A] extends scala.collection.mutable.Iterable[A] {

  def push(elem:A):Unit

  def length:Int


}
