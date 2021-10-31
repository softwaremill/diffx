package com.softwaremill.diffx.specs2

import org.specs2.Specification
import com.softwaremill.diffx.generic.AutoDerivation

class DiffMatcherTest extends Specification with DiffMatcher with AutoDerivation {
  override def is = s2"""This is an empty specification"""

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

  def ignore = left must matchTo(right)
}
sealed trait Parent
case class Bar(s: String, i: Int) extends Parent
case class Foo(bar: Bar, b: List[Int], parent: Option[Parent]) extends Parent
