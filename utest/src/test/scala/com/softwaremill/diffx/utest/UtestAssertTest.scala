package com.softwaremill.diffx.utest

import com.softwaremill.diffx.generic.auto.given
import utest._

object UtestAssertTest extends TestSuite with DiffxAssertions {
  val tests = Tests {

    // uncomment to run
//    test("failing test") {
//      val n = Person("n1", 11)
//      assertEqual(n, Person("n2", 12))
//    }

    test("passing test") {
      val n = Person("n1", 11)
      assertEqual(n, Person("n1", 11))
    }
  }
}

case class Person(name: String, age: Int)
