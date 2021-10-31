package com.softwaremill.diffx.munit

import com.softwaremill.diffx.generic.AutoDerivation

class MunitAssertTest extends munit.FunSuite with DiffxAssertions with AutoDerivation {
//   uncomment to run
//  test("failing test") {
//    val n = Person("n1", 11)
//    assertEqual(n, Person("n2", 12))
//  }

  test("hello") {
    val n = Person("n1", 11)
    assertEqual(n, Person("n1", 11))
  }
}

case class Person(name: String, age: Int)
