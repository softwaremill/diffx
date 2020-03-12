package com.softwaremill.diffx.refined

import com.softwaremill.diffx.{Diff, DiffResultObject, DiffResultString, DiffResultValue, Identical}
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.auto._
import eu.timepit.refined.types.string.NonEmptyString
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RefinedSupportTest extends AnyFlatSpec with Matchers {
  it should "work for refined types" in {
    val testData1 = TestData(1, "foo")
    val testData2 = TestData(1, "bar")
    compare(testData1, testData2) shouldBe DiffResultObject(
      "TestData",
      Map("posInt" -> Identical(1), "nonEmptyString" -> DiffResultString(List(DiffResultValue("foo", "bar"))))
    )
  }

  private def compare[T](t1: T, t2: T)(implicit d: Diff[T]) = d.apply(t1, t2)
}

case class TestData(posInt: PosInt, nonEmptyString: NonEmptyString)
