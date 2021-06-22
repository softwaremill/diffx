package com.softwaremill.diffx.test

import com.softwaremill.diffx._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class DiffResultTest extends AnyFreeSpec with Matchers with DiffxConsoleSupport {
  implicit val colorConfig: ConsoleColorConfig =
    ConsoleColorConfig(default = identity, arrow = identity, right = identity, left = identity)

  "diff set output" - {
    "it should show a simple difference" in {
      val output = DiffResultSet(List(IdenticalValue("a"), DiffResultValue("1", "2"))).show()
      output shouldBe
        s"""Set(
          |     a,
          |     1 -> 2)""".stripMargin
    }

    "it should show an indented difference" in {
      val output =
        DiffResultSet(List(IdenticalValue("a"), DiffResultValue("1", "2"))).show()
      output shouldBe
        s"""Set(
           |     a,
           |     1 -> 2)""".stripMargin
    }

    "it should show a nested list difference" in {
      val output = DiffResultSet(List(IdenticalValue("a"), DiffResultSet(List(IdenticalValue("b"))))).show()
      output shouldBe
        s"""Set(
           |     a,
           |     Set(
           |          b))""".stripMargin
    }

    "it should show null" in {
      val output = DiffResultSet(List(IdenticalValue(null), DiffResultValue(null, null))).show()
      output shouldBe
        s"""Set(
          |     null,
          |     null -> null)""".stripMargin
    }
    "it shouldn't render identical elements" in {
      val output = DiffResultSet(List(IdenticalValue("a"), DiffResultValue("1", "2"))).show(renderIdentical = false)
      output shouldBe
        s"""Set(
           |     1 -> 2)""".stripMargin
    }
  }

  "diff map output" - {
    "it should show a simple diff" in {
      val output =
        DiffResultMap(Map(IdenticalValue("a") -> DiffResultValue(1, 2), DiffResultMissing("b") -> DiffResultMissing(3)))
          .show()
      output shouldBe
        s"""Map(
           |     a: 1 -> 2,
           |     b: 3)""".stripMargin
    }

    "it should show an indented diff" in {
      val output =
        DiffResultMap(Map(IdenticalValue("a") -> DiffResultValue(1, 2), DiffResultMissing("b") -> DiffResultMissing(3)))
          .show()
      output shouldBe
        s"""Map(
           |     a: 1 -> 2,
           |     b: 3)""".stripMargin
    }

    "it should show a nested diff" in {
      val output =
        DiffResultMap(Map(IdenticalValue("a") -> DiffResultMap(Map(IdenticalValue("b") -> DiffResultValue(1, 2)))))
          .show()
      output shouldBe
        s"""Map(
           |     a: Map(
           |          b: 1 -> 2))""".stripMargin
    }

    "shouldn't render identical entries" in {
      val output =
        DiffResultMap(
          Map(
            IdenticalValue("a") -> DiffResultValue(1, 2),
            DiffResultValue("b", "c") -> IdenticalValue(3),
            IdenticalValue("d") -> IdenticalValue(4)
          )
        )
          .show(renderIdentical = false)
      output shouldBe
        s"""Map(
           |     a: 1 -> 2,
           |     b -> c: 3)""".stripMargin
    }
  }

  "diff object output" - {
    "it should show an indented diff with plus and minus signs" in {
      val colorConfigWithPlusMinus: ConsoleColorConfig =
        ConsoleColorConfig(default = identity, arrow = identity, right = s => "+" + s, left = s => "-" + s)

      val output = DiffResultObject(
        "List",
        Map("0" -> DiffResultValue(1234, 123), "1" -> DiffResultMissing(1234), "2" -> IdenticalValue(1234))
      )
        .show()(colorConfigWithPlusMinus)
      output shouldBe
        s"""List(
           |     0: -1234 -> +123,
           |     1: +1234,
           |     2: 1234)""".stripMargin
    }

    "it should not render identical fields" in {
      val output = DiffResultObject(
        "List",
        Map("0" -> DiffResultValue(1234, 123), "1" -> DiffResultMissing(1234), "2" -> IdenticalValue(1234))
      )
        .show(renderIdentical = false)
      output shouldBe
        s"""List(
           |     0: 1234 -> 123,
           |     1: 1234)""".stripMargin
    }
  }
}
