package com.softwaremill.diffx.cats

import cats.data._
import com.softwaremill.diffx._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class DiffxCatsTest extends AnyFreeSpec with Matchers {
  "nonEmptyList" in {
    compare(NonEmptyList.of(1), NonEmptyList.of(2)) shouldBe DiffResultObject(
      "NonEmptyList",
      Map("0" -> DiffResultValue(1, 2))
    )
  }

  "chain" in {
    compare(Chain(1), Chain(2)) shouldBe DiffResultObject(
      "Chain",
      Map("0" -> DiffResultValue(1, 2))
    )
  }

  "nonEmptyChain" in {
    compare(NonEmptyChain.one(1), NonEmptyChain.one(2)) shouldBe DiffResultObject(
      "NonEmptyChain",
      Map("0" -> DiffResultValue(1, 2))
    )
  }

  "nonEmptySet" in {
    compare(NonEmptySet.of(1), NonEmptySet.of(2)) shouldBe DiffResultSet(
      "NonEmptySet",
      Set(DiffResultAdditional(1), DiffResultMissing(2))
    )
  }

  "nonEmptyVector" in {
    compare(NonEmptyVector.of(1), NonEmptyVector.of(2)) shouldBe DiffResultObject(
      "NonEmptyVector",
      Map("0" -> DiffResultValue(1, 2))
    )
  }

  "nonEmptyMap" in {
    compare(NonEmptyMap.of("1" -> 1), NonEmptyMap.of("1" -> 2)) shouldBe DiffResultMap(
      "NonEmptyMap",
      Map(IdenticalValue("1") -> DiffResultValue(1, 2))
    )
  }
}
