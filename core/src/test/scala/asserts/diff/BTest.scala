package asserts.diff

import asserts.diff.Differ._
import org.scalatest.{FlatSpec, Matchers}

class BTest extends FlatSpec with Matchers with DiffMatcher {

  val p1 = Person("kasper", 22)
  val p2 = Person("kasper", 11)

  it should "calculate diff for simple value" in {
    compare(1, 2) shouldBe DiffResultValue(1, 2)
    compare(1, 1) shouldBe Identical2(1)
  }

  it should "calculate diff for product types" in {
    compare(p1, p2) shouldBe DiffResultObject("Person",
                                              Map("name" -> Identical2("kasper"), "age" -> DiffResultValue(22, 11)))
  }

  it should "calculate diff for nested products" in {
    val f1 = Family(p1, p2)
    val f2 = Family(p1, p1)
    compare(f1, f2) shouldBe DiffResultObject(
      "Family",
      Map(
        "first" -> DiffResultObject("Person", Map("name" -> Identical2("kasper"), "age" -> Identical2(22))),
        "second" -> DiffResultObject("Person", Map("name" -> Identical2("kasper"), "age" -> DiffResultValue(11, 22)))
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
            "0" -> DiffResultObject("Person", Map("name" -> Identical2("kasper"), "age" -> Identical2(22))),
            "1" -> DiffResultObject("Person", Map("name" -> Identical2("kasper"), "age" -> DiffResultValue(11, 22))),
            "2" -> DiffResultValue(None, Some(Person("kasper", 22)))
          )
        ))
    )
  }

  it should "show org diff" in {
    val o1 = Organization(List(p1, p2))
    val o2 = Organization(List(p1, p1, p1))
    println(compare(o1, o2).show)
  }

  private def compare[T: DiffFor](t1: T, t2: T) = implicitly[DiffFor[T]].diff(t1, t2)
}

case class Person(name: String, age: Int)

case class Family(first: Person, second: Person)

case class Organization(people: List[Person])
