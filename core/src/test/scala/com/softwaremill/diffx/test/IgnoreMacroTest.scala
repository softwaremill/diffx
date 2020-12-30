package com.softwaremill.diffx.test

import com.softwaremill.diffx.IgnoreMacro
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class IgnoreMacroTest extends AnyFlatSpec with Matchers {
  "IgnoreMacroTest" should "ignore field in nested products" in {
    IgnoreMacro.ignoredFromPath[Family, String](_.first.name) shouldBe List("first", "name")
  }

  it should "ignore fields in list of products" in {
    IgnoreMacro.ignoredFromPath[Organization, String](_.people.each.name) shouldBe List("people", "name")
  }

  it should "ignore fields in product wrapped with either" in {
    IgnoreMacro.ignoredFromPath[Either[Person, Person], String](_.eachRight.name) shouldBe List("name")
    IgnoreMacro.ignoredFromPath[Either[Person, Person], String](_.eachLeft.name) shouldBe List("name")
  }

  it should "ignore fields in product wrapped with option" in {
    IgnoreMacro.ignoredFromPath[Option[Person], String](_.each.name) shouldBe List("name")
  }

  it should "ignore fields in map of products" in {
    IgnoreMacro.ignoredFromPath[Map[String, Person], String](_.each.name) shouldBe List("name")
  }
}
