package com.softwaremill.diffx

import java.time.Instant

import org.scalatest.{FlatSpec, Matchers}

class DiffIgnoreIntTest extends FlatSpec with Matchers with DiffInstances {

  val instant: Instant = Instant.now()
  val p1 = Person("p1", 22, instant)
  val p2 = Person("p2", 11, instant)

  it should "allow importing and exporting implicits" in {
    implicit val d: Diff[Person] = Derived[Diff[Person]].value.ignore(_.name)
    compare(p1, p2) shouldBe DiffResultObject(
      "Person",
      Map("name" -> Identical("p1"), "age" -> DiffResultValue(22, 11), "in" -> Identical(instant))
    )
  }

  it should "allow importing and exporting implicits using macro on derived instance" in {
    implicit val d: Diff[Person] = Derived[Diff[Person]].ignore(_.name)
    compare(p1, p2) shouldBe DiffResultObject(
      "Person",
      Map("name" -> Identical("p1"), "age" -> DiffResultValue(22, 11), "in" -> Identical(instant))
    )
  }

  private def compare[T](t1: T, t2: T)(implicit d: Diff[T]) = d.apply(t1, t2)
}
