package com.softwaremill.diffx.test

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class DiffSemiautoTest extends AnyFreeSpec with Matchers {
  "should compile if all required instances are defined" in {
    assertCompiles("""
        |import com.softwaremill.diffx._
        |final case class P1(f1: String)
        |final case class P2(f1: P1)
        |
        |implicit val p1: Diff[P1] = Diff.derived[P1]
        |implicit val p2: Diff[P2] = Diff.derived[P2]
        |""".stripMargin)
  }

  "should not allow to compile if an instance is missing" in {
    assertDoesNotCompile("""
                     |import com.softwaremill.diffx._
                     |final case class P1(f1: String)
                     |final case class P2(f1: P1)
                     |
                     |implicit val p2: Diff[P2] = Diff.derived[P2]
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
}
