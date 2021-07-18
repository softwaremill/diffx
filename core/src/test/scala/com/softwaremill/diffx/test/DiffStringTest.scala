package com.softwaremill.diffx.test

import com.softwaremill.diffx.{
  DiffResultAdditional,
  DiffResultMissing,
  DiffResultString,
  DiffResultStringLine,
  DiffResultStringWord,
  DiffResultValue,
  IdenticalValue
}
import com.softwaremill.diffx.instances.DiffForString
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class DiffStringTest extends AnyFreeSpec with Matchers {
  val diffForString = new DiffForString

  "identical" - {
    "simple" in {
      val left = "abc"
      val right = "abc"
      diffForString(left, right).isIdentical shouldBe true
    }

    "multiple words" in {
      val left = "abc abc"
      val right = "abc abc"
      diffForString(left, right).isIdentical shouldBe true
    }

    "multiple lines" in {
      val left = "abc\nabc"
      val right = "abc\nabc"
      diffForString(left, right).isIdentical shouldBe true
    }
  }

  "differences" - {
    "lines " - {
      "additional line at the beginning" in {
        val right = "bob\nand mark"
        val left = "alice went to school\nbob\nand mark"
        diffForString(left, right) shouldBe DiffResultString(
          List(DiffResultAdditional("alice went to school"), IdenticalValue("bob"), IdenticalValue("and mark"))
        )
      }

      "additional line in the middle" in {
        val right = "bob\nand mark"
        val left = "bob\nalice went to school\nand mark"
        diffForString(left, right) shouldBe DiffResultString(
          List(IdenticalValue("bob"), DiffResultAdditional("alice went to school"), IdenticalValue("and mark"))
        )
      }

      "additional line at the end" in {
        val right = "bob\nand mark"
        val left = "bob\nand mark\nalice went to school"
        diffForString(left, right) shouldBe DiffResultString(
          List(IdenticalValue("bob"), IdenticalValue("and mark"), DiffResultAdditional("alice went to school"))
        )
      }
    }

    "words" - {
      "single word" in {
        val left = "abc"
        val right = "abd"
        diffForString(left, right) shouldBe DiffResultString(
          List(
            DiffResultStringLine(
              List(DiffResultStringWord(List(IdenticalValue("a"), IdenticalValue("b"), DiffResultValue("c", "d"))))
            )
          )
        )
      }
      "missing word at the end of the line" in {
        val left = "alice bob"
        val right = "alice bob mark"
        diffForString(left, right) shouldBe DiffResultString(
          List(DiffResultStringLine(List(IdenticalValue("alice"), IdenticalValue("bob"), DiffResultMissing("mark"))))
        )
      }

      "missing word at the beginning of the line" in {
        val left = "alice bob"
        val right = "mark alice bob"
        diffForString(left, right) shouldBe DiffResultString(
          List(DiffResultStringLine(List(DiffResultMissing("mark"), IdenticalValue("alice"), IdenticalValue("bob"))))
        )
      }

      "missing word in the middle of the line" in {
        val left = "alice bob"
        val right = "alice mark bob"
        diffForString(left, right) shouldBe DiffResultString(
          List(DiffResultStringLine(List(IdenticalValue("alice"), DiffResultMissing("mark"), IdenticalValue("bob"))))
        )
      }
    }
  }
}
