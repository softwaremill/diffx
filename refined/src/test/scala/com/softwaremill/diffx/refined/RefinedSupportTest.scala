package com.softwaremill.diffx.refined

import com.softwaremill.diffx.{DiffResultObject, DiffResultString, DiffResultValue, IdenticalValue, _}
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._
import com.softwaremill.diffx.generic.auto.diffForCaseClass
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.collection.NonEmpty
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import org.scalatest.EitherValues

class RefinedSupportTest extends AnyFlatSpec with Matchers with EitherValues {
  it should "work for refined types" in {

    /** We have to use refineV here because these test are also run against scala3 where better refined goodies are not
      * yet supported
      */
    val posInt = refineV[Positive](1).value
    val nonEmptyFoo = refineV[NonEmpty]("foo").value
    val nonEmptyBar = refineV[NonEmpty]("bar").value
    val testData1 = TestData(posInt, nonEmptyFoo)
    val testData2 = TestData(posInt, nonEmptyBar)
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
