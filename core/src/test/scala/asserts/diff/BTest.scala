package asserts.diff

import java.time.Instant

import org.scalatest.{FlatSpec, Matchers}

class BTest extends FlatSpec with Matchers with DiffMatcher {

  private val instant: Instant = Instant.now()
  val p1 = Person("kasper", 22, instant)
  val p2 = Person("kasper", 11, instant)

  it should "calculate diff for simple value" in {
    compare(1, 2) shouldBe DiffResultValue(1, 2)
    compare(1, 1) shouldBe Identical(1)
  }

  it should "calculate diff for product types" in {
    compare(p1, p2) shouldBe DiffResultObject(
      "Person",
      Map("name" -> Identical("kasper"), "age" -> DiffResultValue(22, 11), "in" -> Identical(instant)))
  }

  it should "calculate identity for product types" in {
    compare(p1, p1) shouldBe Identical(p1)
  }

  it should "calculate diff for nested products" in {
    val f1 = Family(p1, p2)
    val f2 = Family(p1, p1)
    compare(f1, f2) shouldBe DiffResultObject(
      "Family",
      Map(
        "first" -> Identical(p1),
        "second" -> DiffResultObject("Person",
                                     Map("name" -> Identical("kasper"),
                                         "age" -> DiffResultValue(11, 22),
                                         "in" -> Identical(instant)))
      )
    )
  }

  it should "calculate diff for iterables" in {
    val o1 = Organization(List(p1, p2))
    val o2 = Organization(List(p1, p1, p1))
    compare(o1, o2) shouldBe DiffResultObject(
      "Organization",
      Map(
        "people" -> DiffResultObject(
          "List",
          Map(
            "0" -> Identical(p1),
            "1" -> DiffResultObject("Person",
                                    Map("name" -> Identical("kasper"),
                                        "age" -> DiffResultValue(11, 22),
                                        "in" -> Identical(instant))),
            "2" -> DiffResultMissing(Person("kasper", 22, instant))
          )
        ))
    )
  }

  it should "calculate diff for sealed trait objects" in {
    compare[TsDirection](TsDirection.Outgoing, TsDirection.Incoming) shouldBe DiffResultValue(
      "asserts.diff.TsDirection.Outgoing",
      "asserts.diff.TsDirection.Incoming")
  }

  val right: Foo = Foo(
    Bar("asdf", 5),
    List(123, 1234),
    Some(Bar("asdf", 5))
  )
  val left: Foo = Foo(
    Bar("asdf", 66),
    List(1234),
    Some(right)
  )

  it should "calculate diff for coproduct types" in {
    compare(left, right) shouldBe DiffResultObject(
      "Foo",
      Map(
        "bar" -> DiffResultObject("Bar", Map("s" -> Identical("asdf"), "i" -> DiffResultValue(66, 5))),
        "b" -> DiffResultObject("List", Map("0" -> DiffResultValue(1234, 123), "1" -> DiffResultMissing(1234))),
        "parent" -> DiffResultValue("asserts.diff.Foo", "asserts.diff.Bar")
      )
    )

  }

  it should "work" in {
    left should matchTo(right)
  }

  private def compare[T: DiffFor](t1: T, t2: T) = implicitly[DiffFor[T]].diff(t1, t2)
}

case class Person(name: String, age: Int, in: Instant)

case class Family(first: Person, second: Person)

case class Organization(people: List[Person])
sealed trait Parent
case class Bar(s: String, i: Int) extends Parent
case class Foo(bar: Bar, b: List[Int], parent: Option[Parent]) extends Parent

sealed trait TsDirection
object TsDirection {
  case object Incoming extends TsDirection
  case object Outgoing extends TsDirection
}
