package com.softwaremill.diffx.scalatest

import com.softwaremill.diffx.Diff
import org.scalatest.{FlatSpec, Matchers}

class DiffMatcherTest extends FlatSpec with Matchers with DiffMatcher {

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

  ignore should "work" in {
    left should matchTo(right)
  }

}
sealed trait Parent
case class Bar(s: String, i: Int) extends Parent
case class Foo(bar: Bar, b: List[Int], parent: Option[Parent]) extends Parent
