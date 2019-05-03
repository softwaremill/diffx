package asserts.diff

import org.scalatest.{FlatSpec, Matchers}

class BTest extends FlatSpec with Matchers with DiffMatcher {

  val p1 = Person("kasper", 22)
  val p2 = Person("kasper", 11)

  it should "calculate diff for simple value" in {
    compare(1, 2) shouldBe DiffResultValue(1, 2)
    compare(1, 1) shouldBe Identical(1)
  }

  it should "calculate diff for product types" in {
    compare(p1, p2) shouldBe DiffResultObject("Person",
                                              Map("name" -> Identical("kasper"), "age" -> DiffResultValue(22, 11)))
  }

  it should "calculate diff for nested products" in {
    val f1 = Family(p1, p2)
    val f2 = Family(p1, p1)
    compare(f1, f2) shouldBe DiffResultObject(
      "Family",
      Map(
        "first" -> DiffResultObject("Person", Map("name" -> Identical("kasper"), "age" -> Identical(22))),
        "second" -> DiffResultObject("Person", Map("name" -> Identical("kasper"), "age" -> DiffResultValue(11, 22)))
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
            "0" -> DiffResultObject("Person", Map("name" -> Identical("kasper"), "age" -> Identical(22))),
            "1" -> DiffResultObject("Person", Map("name" -> Identical("kasper"), "age" -> DiffResultValue(11, 22))),
            "2" -> DiffResultMissing(Person("kasper", 22))
          )
        ))
    )
  }

  it should "calculate diff for sealed trait objects" in {
    compare[TsDirection](TsDirection.Outgoing, TsDirection.Incoming) shouldBe DiffResultValue(
      "asserts.diff.TsDirection.Outgoing",
      "asserts.diff.TsDirection.Incoming")
  }

  val left: Foo = Foo(
    Bar("asdf", 5),
    List(123, 1234),
    Some(Bar("asdf", 5))
  )
  val right: Foo = Foo(
    Bar("asdf", 66),
    List(1234),
    Some(left)
  )

  it should "calculate diff for coproduct types" in {
    compare(left, right) shouldBe DiffResultObject(
      "Foo",
      Map(
        "bar" -> DiffResultObject("Bar", Map("s" -> Identical("asdf"), "i" -> DiffResultValue(5, 66))),
        "b" -> DiffResultObject("List", Map("0" -> DiffResultValue(123, 1234), "1" -> DiffResultAdditional(1234))),
        "parent" -> DiffResultValue("asserts.diff.Bar", "asserts.diff.Foo")
      )
    )

  }

  it should "work" in {
    right should matchTo(left)
  }

  private def compare[T: DiffFor](t1: T, t2: T) = implicitly[DiffFor[T]].diff(t1, t2)
}

case class Person(name: String, age: Int)

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
