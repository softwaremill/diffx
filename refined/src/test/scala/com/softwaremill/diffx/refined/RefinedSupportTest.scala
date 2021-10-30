package com.softwaremill.diffx.refined

import com.softwaremill.diffx.{DiffResultObject, DiffResultString, DiffResultValue, IdenticalValue, _}
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.auto._
import com.softwaremill.diffx.refined.refinedDiff
import com.softwaremill.diffx.generic.auto.diffForCaseClass
import eu.timepit.refined.types.string.NonEmptyString
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RefinedSupportTest extends AnyFlatSpec with Matchers {
  it should "work for refined types" in {
    val testData1 = TestData(1, "foo")
    val testData2 = TestData(1, "bar")
    compare(testData1, testData2) shouldBe DiffResultObject(
      "TestData",
      Map(
        "posInt" -> IdenticalValue(1),
        "nonEmptyString" -> DiffResultString(List(DiffResultStringLine(List(DiffResultValue("foo", "bar")))))
      )
    )
  }
}

case class TestData(posInt: PosInt, nonEmptyString: NonEmptyString)
