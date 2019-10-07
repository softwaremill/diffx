package com.softwaremill.diffx.scalatest

import org.specs2.Specification

class HelloWorldSpec extends Specification with DiffMatcher {
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
