package com.softwaremill.diffx.test

import com.softwaremill.diffx.test.ACoproduct.ProductA
import com.softwaremill.diffx.{Derived, Diff, Identical}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class DiffSemiautoTest extends AnyFreeSpec with Matchers {
  "should compile if all required instances are defined" in {
    assertCompiles("""
        |import com.softwaremill.diffx._
        |final case class P1(f1: String)
        |final case class P2(f1: P1)
        |
        |implicit val p1: Derived[Diff[P1]] = Diff.derived[P1]
        |implicit val p2: Derived[Diff[P2]] = Diff.derived[P2]
        |""".stripMargin)
  }

  "should not allow to compile if an instance is missing" in {
    assertDoesNotCompile("""
                     |import com.softwaremill.diffx._
                     |final case class P1(f1: String)
                     |final case class P2(f1: P1)
                     |
                     |implicit val p2: Derived[Diff[P2]] = Diff.derived[P2]
                     |""".stripMargin)
  }

  "should compile with generic.auto._" in {
    assertCompiles("""
                     |import com.softwaremill.diffx._
                     |import com.softwaremill.diffx.generic.auto._
                     |final case class P1(f1: String)
                     |final case class P2(f1: P1)
                     |
                     |val p2: Diff[P2] = Diff[P2]
                     |""".stripMargin)
  }

  "should work for coproducts" in {
    implicit val dACoproduct: Derived[Diff[ACoproduct]] = Diff.derived[ACoproduct]

    Diff.compare[ACoproduct](ProductA("1"), ProductA("1")) shouldBe Identical(
      ProductA("1")
    )
  }

  "should allow ignoring on derived diffs" in {
    implicit val dACoproduct: Derived[Diff[ProductA]] =
      Diff.derived[ProductA].modify[ProductA, String](_.id)(Diff.identical)

    Diff.compare[ProductA](ProductA("1"), ProductA("2")) shouldBe Identical(
      ProductA("1")
    )
  }
}

sealed trait ACoproduct
object ACoproduct {
  case class ProductA(id: String) extends ACoproduct
  case class ProductB(id: String) extends ACoproduct
}
