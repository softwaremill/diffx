package com.softwaremill.diffx.test

import com.softwaremill.diffx.Diff.compare
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
import com.softwaremill.diffx.generic.auto._

import scala.collection.immutable.ListMap

class ObjectMatcherJvmTest extends AnyFreeSpec with Matchers {

  //TODO this should work also on js see: https://github.com/scala-js/scala-js/issues/4538
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
