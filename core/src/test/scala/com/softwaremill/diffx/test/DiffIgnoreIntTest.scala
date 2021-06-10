package com.softwaremill.diffx.test

import com.softwaremill.diffx._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.softwaremill.diffx.generic.auto._

import java.time.Instant

class DiffIgnoreIntTest extends AnyFlatSpec with Matchers {
  val instant: Instant = Instant.now()
  val p1 = Person("p1", 22, instant)
  val p2 = Person("p2", 11, instant)

  it should "allow importing and exporting implicits" in {
    implicit val d: Diff[Person] = Derived[Diff[Person]].modify[Person, String](_.name)(Diff.identical[String])
    compare(p1, p2) shouldBe DiffResultObject(
      "Person",
      Map("name" -> Identical("p1"), "age" -> DiffResultValue(22, 11), "in" -> Identical(instant))
    )
  }

  it should "allow importing and exporting implicits using macro on derived instance" in {
    implicit val d: Diff[Person] = Derived[Diff[Person]].modify[Person, String](_.name)(Diff.identical)
    compare(p1, p2) shouldBe DiffResultObject(
      "Person",
      Map("name" -> Identical("p1"), "age" -> DiffResultValue(22, 11), "in" -> Identical(instant))
    )
  }

  it should "allow calling ignore multiple times" in {
    implicit val d: Diff[Person] =
      Derived[Diff[Person]]
        .modify[Person, String](_.name)(Diff.identical)
        .modify[Person, Int](_.age)(Diff.identical)
    compare(p1, p2) shouldBe Identical(p1)
  }
}
