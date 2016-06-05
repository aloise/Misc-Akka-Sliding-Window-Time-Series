import name.aloise.data.SlidingSeq
import org.scalatest._

/**
  * User: aloise
  * Date: 05.06.16
  * Time: 18:50
  */
class SlidingSeqSpec extends FlatSpec with BeforeAndAfterAll with ShouldMatchers {

  "Sliding Seq" should "return an empty set for empty input" in {
    val s = new SlidingSeq[Int]( _ - _ > 10 )

    s.toSeq shouldBe empty

  }

  it should "return a correct set for every pushed element" in {
    val s = new SlidingSeq[Int]( _ - _ < 3 )

    s.push(1)

    s.toSeq shouldBe Seq( 1 )

    s.push(2)

    s.toSeq shouldBe Seq( 2, 1 )

    s.push(3)

    s.toSeq shouldBe Seq( 3, 2, 1)

    s.push(4)

    s.toSeq shouldBe Seq( 4, 3, 2 )

    s.push(5)

    s.toSeq shouldBe Seq( 5, 4, 3 )

    s.push(10)

    s.toSeq shouldBe Seq( 10 )
  }

}
