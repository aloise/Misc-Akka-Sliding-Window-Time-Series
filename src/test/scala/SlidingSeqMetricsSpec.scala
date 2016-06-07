
import name.aloise.data._
import name.aloise.models.InputValue
import org.scalatest._

/**
  * User: aloise
  * Date: 06.06.16
  * Time: 11:56
  */
class SlidingSeqMetricsSpec extends FlatSpec with BeforeAndAfterAll with ShouldMatchers {


  "Sliding Sequence with calculated metrics" should "return empty stats" in {

    val s = new SlidingSeqMetrics[Int]( _ - _ > 10 )

    s.toSeq shouldBe empty
  }

  it should "compute stats correctly" in {
    val s = new SlidingSeqMetrics[Int]( _ - _ < 3 )

    s.push(1)
    s.push(2)
    s.push(3)
    s.push(4)
    s.toSeq shouldBe Seq( 4, 3, 2 )
    s.min shouldBe 2
    s.max shouldBe 4
    s.sum shouldBe 9
    s.num shouldBe 3

    s.push(5)
    s.toSeq shouldBe Seq( 5, 4, 3 )
    s.min shouldBe 3
    s.max shouldBe 5
    s.sum shouldBe 12

    s.push(10)
    s.toSeq shouldBe Seq( 10 )
    s.min shouldBe 10
    s.max shouldBe 10
    s.sum shouldBe 10
    s.num shouldBe 1

    s.push(11)
    s.push(12)
    s.toSeq shouldBe Seq( 12, 11, 10 )
    s.min shouldBe 10
    s.max shouldBe 12
    s.sum shouldBe 33
    s.num shouldBe 3

  }

  it should "work with custom data types" in {

    case class SampleInputValue( override val timestamp:Int, override val value:BigDecimal ) extends InputValue( timestamp, value )

    val ordering = new Ordering[SampleInputValue]() {
      override def compare(x: SampleInputValue, y: SampleInputValue): Int = x.value.compare( y.value )
    }

    val s = new SlidingSeqMetrics[SampleInputValue]( _.timestamp - _.timestamp < 10 )( ordering, x => x.value )

    s.push( SampleInputValue( 1, 0.5 ) )
    s.toSeq shouldBe Seq( SampleInputValue( 1, 0.5 ) )
    s.sum shouldBe BigDecimal("0.5")
    s.min.value shouldBe BigDecimal("0.5")
    s.max.value shouldBe BigDecimal("0.5")
    s.num shouldBe 1

    s.push( SampleInputValue( 2, BigDecimal( "1" ) ) )
    s.num shouldBe 2
    s.push( SampleInputValue( 3, BigDecimal( "1.5" ) ) )
    s.num shouldBe 3

    s.sum shouldBe BigDecimal("3")
    s.min.value shouldBe BigDecimal("0.5")
    s.max.value shouldBe BigDecimal("1.5")


    s.push( SampleInputValue( 12, BigDecimal( "10") ) )
    s.num shouldBe 2
    s.min.value shouldBe BigDecimal("1.5")
    s.max.value shouldBe BigDecimal("10")
    s.sum shouldBe BigDecimal("11.5")

    s.push( SampleInputValue( 14, BigDecimal( "0.22") ) )
    s.push( SampleInputValue( 18, BigDecimal( "1") ) )

    s.toSeq.map( _.timestamp ) shouldBe Seq( 18, 14, 12 )
    s.num shouldBe 3
    s.min.value shouldBe BigDecimal("0.22")
    s.max.value shouldBe BigDecimal("10")
    s.sum shouldBe BigDecimal("11.22")

  }

}
