package com.softwaremill.diffx

import java.time.Instant

import org.scalatest.{FlatSpec, Matchers}

class DiffTest extends FlatSpec with Matchers with DiffForInstances {

  private val instant: Instant = Instant.now()
  val p1 = Person("p1", 22, instant)
  val p2 = Person("p2", 11, instant)

  it should "calculate diff for simple value" in {
    compare(1, 2) shouldBe DiffResultValue(1, 2)
    compare(1, 1) shouldBe Identical(1)
  }

  it should "calculate diff for product types" in {
    compare(p1, p2) shouldBe DiffResultObject(
      "Person",
      Map(
        "name" -> DiffResultValue(p1.name, p2.name),
        "age" -> DiffResultValue(p1.age, p2.age),
        "in" -> Identical(instant)
      )
    )
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
        "second" -> DiffResultObject(
          "Person",
          Map(
            "name" -> DiffResultValue(p2.name, p1.name),
            "age" -> DiffResultValue(p2.age, p1.age),
            "in" -> Identical(instant)
          )
        )
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
            "1" -> DiffResultObject(
              "Person",
              Map(
                "name" -> DiffResultValue(p2.name, p1.name),
                "age" -> DiffResultValue(p2.age, p1.age),
                "in" -> Identical(instant)
              )
            ),
            "2" -> DiffResultMissing(Person(p1.name, p1.age, instant))
          )
        )
      )
    )
  }

  it should "calculate diff for sealed trait objects" in {
    compare[TsDirection](TsDirection.Outgoing, TsDirection.Incoming) shouldBe DiffResultValue(
      "com.softwaremill.diffx.TsDirection.Outgoing",
      "com.softwaremill.diffx.TsDirection.Incoming"
    )
  }

  it should "calculate diff for sets" in {
    val diffResult = compare(Set(1, 2, 3, 4, 5), Set(1, 2, 3, 4)).asInstanceOf[DiffResultSet]
    diffResult.diffs should contain theSameElementsAs List(
      DiffResultAdditional(5),
      Identical(4),
      Identical(3),
      Identical(2),
      Identical(1)
    )
  }

  it should "calculate diff for mutable sets" in {
    import scala.collection.{Set => mSet}
    val diffResult = compare(mSet(1, 2, 3, 4, 5), mSet(1, 2, 3, 4)).asInstanceOf[DiffResultSet]
    diffResult.diffs should contain theSameElementsAs List(
      DiffResultAdditional(5),
      Identical(4),
      Identical(3),
      Identical(2),
      Identical(1)
    )
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
        "parent" -> DiffResultValue("com.softwaremill.diffx.Foo", "com.softwaremill.diffx.Bar")
      )
    )

  }

  it should "calculate diff for set of products" in {
    val p2m = p2.copy(age = 33)
    compare(Set(p1, p2), Set(p1, p2m)) shouldBe DiffResultSet(
      List(DiffResultAdditional(p2), DiffResultMissing(p2m), Identical(p1))
    )
  }

  it should "calculate diff for set of products using instance matcher" in {
    val p2m = p2.copy(age = 33)
    implicit val im: EntityMatcher[Person] = (left: Person, right: Person) => left.name == right.name
    compare(Set(p1, p2), Set(p1, p2m)) shouldBe DiffResultSet(
      List(
        Identical(p1),
        DiffResultObject(
          "Person",
          Map("name" -> Identical(p2.name), "age" -> DiffResultValue(p2.age, p2m.age), "in" -> Identical(p1.in))
        )
      )
    )
  }

  private def compare[T: DiffFor](t1: T, t2: T) = implicitly[DiffFor[T]].apply(t1, t2)
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