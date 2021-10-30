package com.softwaremill.diffx.test

import com.softwaremill.diffx._
import org.scalatest.matchers.should.Matchers

import java.time.Instant
import scala.collection.immutable.ListMap
import com.softwaremill.diffx.generic.auto.diffForCaseClass

trait DiffVersionSpecificTest { self: DiffTest =>
  "recursive coproducts" - {
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

    "identity" in {
      compare(left, left).isIdentical shouldBe true
    }

    "diff" in {
      compare(left, right) shouldBe DiffResultObject(
        "Foo",
        ListMap(
          "bar" -> DiffResultObject("Bar", ListMap("s" -> IdenticalValue("asdf"), "i" -> DiffResultValue(66, 5))),
          "b" -> DiffResultObject("List", ListMap("0" -> DiffResultValue(1234, 123), "1" -> DiffResultMissing(1234))),
          "parent" -> DiffResultValue("com.softwaremill.diffx.test.Foo", "com.softwaremill.diffx.test.Bar")
        )
      )
    }
  }
  "non case class instance" - {
    "identical" in {
      compare(new NonCaseClass("a"), new NonCaseClass("a")).isIdentical shouldBe true
    }
    "diff" in {
      compare(new NonCaseClass("a"), new NonCaseClass("b")).isIdentical shouldBe false
    }
  }
}
