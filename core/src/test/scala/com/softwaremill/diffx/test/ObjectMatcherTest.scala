package com.softwaremill.diffx.test

import com.softwaremill.diffx.Diff.compare
import com.softwaremill.diffx.generic.auto._
import com.softwaremill.diffx.{
  DiffResultAdditional,
  DiffResultMissing,
  DiffResultObject,
  DiffResultValue,
  IdenticalValue,
  ObjectMatcher
}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.immutable.ListMap

class ObjectMatcherTest extends AnyFreeSpec with Matchers {

  "list" - {
    "list full of duplicates should be equal to itself" in {
      val left = List(Example(1, 1), Example(1, 1), Example(1, 1), Example(1, 1), Example(1, 1))
      implicit val om = ObjectMatcher.list[Example].byValue(_.a)
      compare(left, left).isIdentical shouldBe true
    }

    "should prefer identical matches when there are multiple possible choices" in {
      val left = List(Example(1, 1), Example(1, 2))
      val right = List(Example(1, 2), Example(1, 1))
      implicit val om = ObjectMatcher.list[Example].byValue(_.a)
      compare(left, right).isIdentical shouldBe true
    }

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
          "0" -> DiffResultObject("Example", ListMap("a" -> IdenticalValue(1), "b" -> DiffResultValue(1, -1))),
          "1" -> DiffResultObject("Example", ListMap("a" -> IdenticalValue(2), "b" -> DiffResultValue(2, -2)))
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
          "0" -> DiffResultObject("OptionalExample", ListMap("a" -> IdenticalValue(1), "b" -> DiffResultValue(1, -1))),
          "1" -> DiffResultObject(
            "OptionalExample",
            ListMap("a" -> IdenticalValue(None), "b" -> DiffResultValue(2, -2))
          )
        )
      )
    }

    "should preserve order even if there are missing or additional entities" in {
      val left = List(Example(2, 2), Example(3, 3), Example(4, 4))
      val right = List(Example(0, 0), Example(1, -1), Example(2, -2))
      implicit val om = ObjectMatcher.list[Example].byValue(_.a)
      compare(left, right) shouldBe DiffResultObject(
        "List",
        ListMap(
          "0" -> DiffResultMissing(Example(0, 0)),
          "1" -> DiffResultMissing(Example(1, -1)),
          "2" -> DiffResultObject("Example", ListMap("a" -> IdenticalValue(2), "b" -> DiffResultValue(2, -2))),
          "3" -> DiffResultAdditional(Example(3, 3)),
          "4" -> DiffResultAdditional(Example(4, 4))
        )
      )
    }

    "should preserve order even if there are missing or additional entities - 2" in {
      val left = List(Example(1, 1), Example(3, 3), Example(4, 4))
      val right = List(Example(0, 0), Example(1, -1), Example(2, -2), Example(3, -3))
      implicit val om = ObjectMatcher.list[Example].byValue(_.a)
      compare(left, right) shouldBe DiffResultObject(
        "List",
        ListMap(
          "0" -> DiffResultMissing(Example(0, 0)),
          "1" -> DiffResultObject("Example", ListMap("a" -> IdenticalValue(1), "b" -> DiffResultValue(1, -1))),
          "2" -> DiffResultMissing(Example(2, -2)),
          "3" -> DiffResultObject("Example", ListMap("a" -> IdenticalValue(3), "b" -> DiffResultValue(3, -3))),
          "4" -> DiffResultAdditional(Example(4, 4))
        )
      )
    }

    "should preserve order even if there are missing or additional entities - 3" in {
      val left = List(Example(1, 1), Example(2, -2), Example(3, 3), Example(4, 4))
      val right = List(Example(0, 0), Example(1, -1), Example(3, -3))
      implicit val om = ObjectMatcher.list[Example].byValue(_.a)
      compare(left, right) shouldBe DiffResultObject(
        "List",
        ListMap(
          "0" -> DiffResultMissing(Example(0, 0)),
          "1" -> DiffResultObject("Example", ListMap("a" -> IdenticalValue(1), "b" -> DiffResultValue(1, -1))),
          "2" -> DiffResultAdditional(Example(2, -2)),
          "3" -> DiffResultObject("Example", ListMap("a" -> IdenticalValue(3), "b" -> DiffResultValue(3, -3))),
          "4" -> DiffResultAdditional(Example(4, 4))
        )
      )
    }

    "should preserve order even if there are missing or additional entities - 4" in {
      val left = List(Example(0, 0), Example(1, -1), Example(2, 2))
      val right = List(Example(2, -2), Example(3, 3), Example(4, 4))
      implicit val om = ObjectMatcher.list[Example].byValue(_.a)
      compare(left, right) shouldBe DiffResultObject(
        "List",
        ListMap(
          "0" -> DiffResultAdditional(Example(0, 0)),
          "1" -> DiffResultAdditional(Example(1, -1)),
          "2" -> DiffResultObject("Example", ListMap("a" -> IdenticalValue(2), "b" -> DiffResultValue(2, -2))),
          "3" -> DiffResultMissing(Example(3, 3)),
          "4" -> DiffResultMissing(Example(4, 4))
        )
      )
    }

    "should preserve order even if there are missing or additional entities - cross match" in {
      val left = List(Example(1, 1), Example(0, 0), Example(2, 2))
      val right = List(Example(2, -2), Example(3, 3), Example(1, -1))
      implicit val om = ObjectMatcher.list[Example].byValue(_.a)
      compare(left, right) shouldBe DiffResultObject(
        "List",
        ListMap(
          "0" -> DiffResultObject("Example", ListMap("a" -> IdenticalValue(2), "b" -> DiffResultValue(2, -2))),
          "1" -> DiffResultMissing(Example(3, 3)),
          "2" -> DiffResultObject("Example", ListMap("a" -> IdenticalValue(1), "b" -> DiffResultValue(1, -1))),
          "3" -> DiffResultAdditional(Example(0, -0))
        )
      )
    }

    "should preserve order even if there are missing or additional entities and duplicates" in {
      val left = List(Example(2, 2), Example(3, 3), Example(4, 4))
      val right = List(Example(0, 0), Example(1, -1), Example(2, -2), Example(2, -2))
      implicit val om = ObjectMatcher.list[Example].byValue(_.a)
      compare(left, right) shouldBe DiffResultObject(
        "List",
        ListMap(
          "0" -> DiffResultMissing(Example(0, 0)),
          "1" -> DiffResultMissing(Example(1, -1)),
          "2" -> DiffResultObject("Example", ListMap("a" -> IdenticalValue(2), "b" -> DiffResultValue(2, -2))),
          "3" -> DiffResultAdditional(Example(3, 3)),
          "4" -> DiffResultAdditional(Example(4, 4)),
          "5" -> DiffResultMissing(Example(2, -2))
        )
      )
    }
  }
  "set" - {
    "set full of duplicates according to object matcher should be identical to itself" in {
      val left = Set(Example(1, 1), Example(1, 2), Example(1, 3), Example(1, 4), Example(1, 5))
      implicit val om = ObjectMatcher.set[Example].by(_.a)

      val result = compare(left, left)
      result.isIdentical shouldBe true
    }

    "should prefer identical matches when there are multiple possible choices" in {
      val left = Set(Example(1, 1), Example(1, 2))
      val right = Set(Example(1, 2), Example(1, 1))
      implicit val om = ObjectMatcher.set[Example].by(_.a)
      compare(left, right).isIdentical shouldBe true
    }
  }

  "map" - {
    "map full of duplicates according to object matcher should be identical to itself" in {
      val left = Map(1 -> Example(1, 1), 2 -> Example(1, 2), 3 -> Example(1, 3), 4 -> Example(1, 4), 5 -> Example(1, 5))
      implicit val om = ObjectMatcher.map[Int, Example].byValue(_.a)

      val result = compare(left, left)
      result.isIdentical shouldBe true
    }

    "should prefer identical matches when there are multiple possible choices" in {
      val left = Set(Example(1, 1), Example(1, 2))
      val right = Set(Example(1, 2), Example(1, 1))
      implicit val om = ObjectMatcher.set[Example].by(_.a)
      compare(left, right).isIdentical shouldBe true
    }
  }
}

case class Example(a: Int, b: Int)
case class OptionalExample(a: Option[Int], b: Int)
