package com.softwaremill.diffx.test

import com.softwaremill.diffx.Diff.compare
import com.softwaremill.diffx.generic.auto._
import com.softwaremill.diffx.{DiffResultObject, DiffResultValue, IdenticalValue, ObjectMatcher}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.immutable.ListMap

class ObjectMatcherTest extends AnyFreeSpec with Matchers {

  "should match list by value using whole object" in {
    val left = List(Example(1, 1), Example(2, 2))
    implicit val om = ObjectMatcher.list[Example].byValue
    compare(left, left.reverse).isIdentical shouldBe true
  }

  "should match list by value using object property" in {
    val left = List(Example(2, 2), Example(1, 1))
    val right = List(Example(1, -1), Example(2, -2))
    implicit val om = ObjectMatcher.list[Example].byValue(_.a)
    compare(left, right) shouldBe DiffResultObject(
      "List",
      ListMap(
        "0" -> DiffResultObject("Example", ListMap("a" -> IdenticalValue(2), "b" -> DiffResultValue(2, -2))),
        "1" -> DiffResultObject("Example", ListMap("a" -> IdenticalValue(1), "b" -> DiffResultValue(1, -1)))
      )
    )
  }

  "should match list by value using object optional property" in {
    val left = List(OptionalExample(None, 2), OptionalExample(Some(1), 1))
    val right = List(OptionalExample(Some(1), -1), OptionalExample(None, -2))
    implicit val om = ObjectMatcher.list[OptionalExample].byValue(_.a)
    compare(left, right) shouldBe DiffResultObject(
      "List",
      ListMap(
        "0" -> DiffResultObject("OptionalExample", ListMap("a" -> IdenticalValue(None), "b" -> DiffResultValue(2, -2))),
        "1" -> DiffResultObject("OptionalExample", ListMap("a" -> IdenticalValue(1), "b" -> DiffResultValue(1, -1)))
      )
    )
  }
}

case class Example(a: Int, b: Int)
case class OptionalExample(a: Option[Int], b: Int)
