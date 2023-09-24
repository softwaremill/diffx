package com.softwaremill.diffx.weaver

import cats.data.Validated
import com.softwaremill.diffx.generic.AutoDerivation
import weaver.FunSuite

object DiffxExpectationsTest extends FunSuite with DiffxExpectations with AutoDerivation {
  test("expectEqual should fail when there are differences") {
    val n = Person("n1", 11)
    expectEqual(n, Person("n2", 12)).run match {
      case Validated.Valid(_)   => failure(s"Expected a failure but succeeded")
      case Validated.Invalid(_) => success
    }
  }

  test("expectEqual should succeed when there are no differences") {
    val n = Person("n1", 11)
    expectEqual(n, Person("n1", 11))
  }
}

case class Person(name: String, age: Int)
