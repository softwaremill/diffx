package com.softwaremill.diffx

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class DiffResultTest extends AnyFreeSpec with Matchers with DiffxConsoleSupport {
  implicit val colorConfig: ConsoleColorConfig =
    ConsoleColorConfig.dark.copy(default = identity, arrow = identity, right = identity, left = identity)

  "diff set output" - {
    "it should show a simple difference" in {
      val output = DiffResultSet(List(Identical("a"), DiffResultValue("1", "2"))).show
      output shouldBe
        s"""Set(
          |     a,
          |     1 -> 2)""".stripMargin
    }

    "it should show an indented difference" in {
      val output = DiffResultSet(List(Identical("a"), DiffResultValue("1", "2"))).showIndented(5)
      output shouldBe
        s"""Set(
           |     a,
           |     1 -> 2)""".stripMargin
    }

    "it should show a nested list difference" in {
      val output = DiffResultSet(List(Identical("a"), DiffResultSet(List(Identical("b"))))).show
      output shouldBe
        s"""Set(
           |     a,
           |     Set(
           |          b))""".stripMargin
    }

    "it should show null" in {
      val output = DiffResultSet(List(Identical(null), DiffResultValue(null, null))).show
      output shouldBe
        s"""Set(
          |     null,
          |     null -> null)""".stripMargin
    }
  }

  "diff map output" - {
    "it should show a simple diff" in {
      val output =
        DiffResultMap(Map(Identical("a") -> DiffResultValue(1, 2), DiffResultMissing("b") -> DiffResultMissing(3))).show
      output shouldBe
        s"""Map(
           |     a: 1 -> 2,
           |     b: 3)""".stripMargin
    }

    "it should show an indented diff" in {
      val output =
        DiffResultMap(Map(Identical("a") -> DiffResultValue(1, 2), DiffResultMissing("b") -> DiffResultMissing(3)))
          .showIndented(5)
      output shouldBe
        s"""Map(
           |     a: 1 -> 2,
           |     b: 3)""".stripMargin
    }

    "it should show a nested diff" in {
      val output =
        DiffResultMap(Map(Identical("a") -> DiffResultMap(Map(Identical("b") -> DiffResultValue(1, 2))))).show
      output shouldBe
        s"""Map(
           |     a: Map(
           |          b: 1 -> 2))""".stripMargin
    }
  }
}
