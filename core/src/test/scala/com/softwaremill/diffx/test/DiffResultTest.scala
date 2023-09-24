package com.softwaremill.diffx.test

import com.softwaremill.diffx._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class DiffResultTest extends AnyFreeSpec with Matchers {
  implicit val showConfig: ShowConfig = ShowConfig.noColors

  "diff set output" - {
    "it should show a simple difference" in {
      val output = DiffResultSet("Set", Set(IdenticalValue("a"), DiffResultValue("1", "2"))).show()
      output shouldBe
        s"""Set(
           |     a,
           |     1 -> 2)""".stripMargin
    }

    "it should show an indented difference" in {
      val output =
        DiffResultSet("Set", Set(IdenticalValue("a"), DiffResultValue("1", "2"))).show()
      output shouldBe
        s"""Set(
           |     a,
           |     1 -> 2)""".stripMargin
    }

    "it should show a nested list difference" in {
      val output = DiffResultSet("Set", Set(IdenticalValue("a"), DiffResultSet("Set", Set(IdenticalValue("b"))))).show()
      output shouldBe
        s"""Set(
           |     a,
           |     Set(
           |          b))""".stripMargin
    }

    "it should show null" in {
      val output = DiffResultSet("Set", Set(IdenticalValue(null), DiffResultValue(null, null))).show()
      output shouldBe
        s"""Set(
           |     null,
           |     null -> null)""".stripMargin
    }
    "it shouldn't render identical elements" in {
      val output =
        DiffResultSet("Set", Set(IdenticalValue("a"), DiffResultValue("1", "2")))
          .show()(showConfig.skipIdentical)
      output shouldBe
        s"""Set(
           |     1 -> 2)""".stripMargin
    }
  }

  "diff map output" - {
    "it should show a simple diff" in {
      val output =
        DiffResultMap(
          "Map",
          Map(IdenticalValue("a") -> DiffResultValue(1, 2), DiffResultMissing("b") -> DiffResultMissing(3))
        )
          .show()
      output shouldBe
        s"""Map(
           |     a: 1 -> 2,
           |     -b: -3)""".stripMargin
    }

    "it should show an indented diff" in {
      val output =
        DiffResultMap(
          "Map",
          Map(IdenticalValue("a") -> DiffResultValue(1, 2), DiffResultMissing("b") -> DiffResultMissing(3))
        )
          .show()
      output shouldBe
        s"""Map(
           |     a: 1 -> 2,
           |     -b: -3)""".stripMargin
    }

    "it should show a nested diff" in {
      val output =
        DiffResultMap(
          "Map",
          Map(IdenticalValue("a") -> DiffResultMap("Map", Map(IdenticalValue("b") -> DiffResultValue(1, 2))))
        )
          .show()
      output shouldBe
        s"""Map(
           |     a: Map(
           |          b: 1 -> 2))""".stripMargin
    }

    "shouldn't render identical entries" in {
      val output =
        DiffResultMap(
          "Map",
          Map(
            IdenticalValue("a") -> DiffResultValue(1, 2),
            DiffResultValue("b", "c") -> IdenticalValue(3),
            IdenticalValue("d") -> IdenticalValue(4)
          )
        )
          .show()(showConfig.skipIdentical)
      output shouldBe
        s"""Map(
           |     a: 1 -> 2,
           |     b -> c: 3)""".stripMargin
    }
  }

  "diff object output" - {
    "it should show an indented diff with plus and minus signs" in {
      val colorConfigWithPlusMinus: ShowConfig = ShowConfig.noColors.copy(left = s => s"-$s", right = s => s"+$s")

      val output = DiffResultObject(
        "List",
        Map("0" -> DiffResultValue(1234, 123), "1" -> DiffResultMissing(1234), "2" -> IdenticalValue(1234))
      )
        .show()(colorConfigWithPlusMinus)
      output shouldBe
        s"""List(
           |     0: -1234 -> +123,
           |     1: -1234,
           |     2: 1234)""".stripMargin
    }

    "it should not render identical fields" in {
      val output = DiffResultObject(
        "List",
        Map("0" -> DiffResultValue(1234, 123), "1" -> DiffResultMissing(1234), "2" -> IdenticalValue(1234))
      )
        .show()(showConfig.skipIdentical)
      output shouldBe
        s"""List(
           |     0: 1234 -> 123,
           |     1: -1234)""".stripMargin
    }

    "multiple consecutive different characters should be grouped into a single chunk" in {
      val output = DiffResultStringWord(
        List(
          IdenticalValue("p"),
          IdenticalValue("e"),
          IdenticalValue("r"),
          DiffResultChunk("s", "b"),
          DiffResultChunk("o", "a"),
          DiffResultChunk("n", "m"),
          IdenticalValue("e"),
          IdenticalValue("s")
        )
      ).show()
      output shouldBe "per[son -> bam]es"
    }

    "display missing space character" in {
      DiffResultString(
        List(
          DiffResultStringLine(
            List(
              IdenticalValue("abc"),
              IdenticalValue(" "),
              IdenticalValue("abc"),
              DiffResultMissingChunk(" ")
            )
          )
        )
      ).show() shouldBe "abc abc-[ ]"
    }
  }

  "diff iterable output" - {
    "it should show an indented diff for objects with multiline toString" in {
      val john = new VerboseNonCaseClass("John", 33)
      val mary = new VerboseNonCaseClass("Mary", 28)
      val jane = new VerboseNonCaseClass("Jane", 5)
      DiffResultIterable(
        "List",
        Map(
          "0" -> IdenticalValue(john),
          "1" -> DiffResultAdditional(mary),
          "2" -> DiffResultMissing(jane)
        )
      ).show() shouldBe
        """List(
          |     0: VerboseNonCaseClass(
          |              key:   John,
          |              value: 33
          |          ),
          |     1: +VerboseNonCaseClass(
          |          +    key:   Mary,
          |          +    value: 28
          |          +),
          |     2: -VerboseNonCaseClass(
          |          -    key:   Jane,
          |          -    value: 5
          |          -))""".stripMargin

    }
  }
}
