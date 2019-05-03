package asserts.diff

import org.scalatest.{FlatSpec, Matchers}
import Differ._

class BTest extends FlatSpec with Matchers with DiffMatcher {

  val p1 = Person("kasper", 22)
  val p2 = Person("kasper", 11)

  it should "asd" in {
    implicitly[DiffFor[Int]].diff(1, 2)
    implicitly[DiffFor[Person]].diff(p1, p2)
  }

  it should "work1" in {
    println(compare(p1, p2).show(5))
  }

  it should "work" in {
    val f1 = Family(p1, p2)
    val f2 = Family(p1, p1)
    println(compare(f1, f2).show(5))
  }

  it should "work for org" in {
    val o1 = Organization(List(p1, p2))
    val o2 = Organization(List(p1, p1, p1))
    println(compare(o1, o2).show(5))
  }

  private def compare[T: DiffFor](t1: T, t2: T) = implicitly[DiffFor[T]].diff(t1, t2)
}

case class Person(name: String, age: Int)
case class Family(first: Person, second: Person)
case class Organization(people: List[Person])
