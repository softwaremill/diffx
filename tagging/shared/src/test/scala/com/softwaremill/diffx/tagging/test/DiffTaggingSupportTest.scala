package com.softwaremill.diffx.tagging.test

import com.softwaremill.diffx.{Diff, DiffResultObject, DiffResultValue, Identical}
import com.softwaremill.diffx.tagging._
import com.softwaremill.tagging._
import com.softwaremill.tagging.@@
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DiffTaggingSupportTest extends AnyFlatSpec with Matchers {
  it should "work for tagged types" in {
    val p1 = 1.taggedWith[T1]
    val p11 = 2.taggedWith[T1]
    val p2 = 1.taggedWith[T2]
    compare(TestData(p1, p2), TestData(p11, p2)) shouldBe DiffResultObject(
      "TestData",
      Map("p1" -> DiffResultValue(p1, p11), "p2" -> Identical(p2))
    )
  }

  private def compare[T](t1: T, t2: T)(implicit d: Diff[T]) = d.apply(t1, t2)
}

sealed trait T1
sealed trait T2
case class TestData(p1: Int @@ T1, p2: Int @@ T2)
