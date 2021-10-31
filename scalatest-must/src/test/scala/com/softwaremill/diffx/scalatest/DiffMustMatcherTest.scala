package com.softwaremill.diffx.scalatest

import com.softwaremill.diffx.generic.AutoDerivation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class DiffMatcherTest extends AnyFlatSpec with Matchers with DiffMustMatcher with AutoDerivation {
  val right: Foo = Foo(
    Bar("asdf", 5, Map("a" -> 2)),
    List(123, 1234)
  )
  val left: Foo = Foo(
    Bar("asdf", 66, Map("b" -> 3)),
    List(1234)
  )

  ignore should "work" in {
    left mustMatchTo (right)
  }

  it should "work with option and some" in {
    Option("test") mustMatchTo (Some("test"))
  }
}
sealed trait Parent
case class Bar(s: String, i: Int, ss: Map[String, Int]) extends Parent
case class Foo(bar: Bar, b: List[Int]) extends Parent
