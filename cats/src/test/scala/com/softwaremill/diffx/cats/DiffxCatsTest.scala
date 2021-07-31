package com.softwaremill.diffx.cats

import cats.data._
import com.softwaremill.diffx._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class DiffxCatsTest extends AnyFreeSpec with Matchers {
  "nonEmptyList" in {
    compare(NonEmptyList.of(1), NonEmptyList.of(2)) shouldBe DiffResultObject("List", Map("0" -> DiffResultValue(1, 2)))
  }

  "nonEmptyChain" in {
    compare(NonEmptyChain.one(1), NonEmptyChain.one(2)) shouldBe DiffResultObject(
      "List",
      Map("0" -> DiffResultValue(1, 2))
    )
  }

  "nonEmptySet" in {
    compare(NonEmptySet.of(1), NonEmptySet.of(2)) shouldBe DiffResultSet(
      Set(DiffResultAdditional(1), DiffResultMissing(2))
    )
  }

  "nonEmptyVector" in {
    compare(NonEmptyVector.of(1), NonEmptyVector.of(2)) shouldBe DiffResultObject(
      "List",
      Map("0" -> DiffResultValue(1, 2))
    )
  }
}
