package com.softwaremill.diffx.test

import com.softwaremill.diffx.instances.DiffForString
import com.softwaremill.diffx.{
  DiffResultAdditional,
  DiffResultAdditionalChunk,
  DiffResultChunk,
  DiffResultMissingChunk,
  DiffResultString,
  DiffResultStringLine,
  DiffResultStringWord,
  DiffResultValue,
  IdenticalValue
}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class DiffStringTest extends AnyFreeSpec with Matchers {
  val diffForString = new DiffForString

  "characters" - {
    "identical" - {
      val left = "abc"
      val right = "abc"
      diffForString(left, right).isIdentical shouldBe true
    }
    "single difference" in {
      val left = "abc"
      val right = "abd"
      diffForString(left, right) shouldBe DiffResultString(
        List(
          DiffResultStringLine(
            List(DiffResultStringWord(List(IdenticalValue("a"), IdenticalValue("b"), DiffResultChunk("c", "d"))))
          )
        )
      )
    }

    "subset" in {
      val left = "abcdefgh"
      val right = "abcd"
      diffForString(left, right) shouldBe DiffResultString(
        List(
          DiffResultStringLine(
            List(
              DiffResultStringWord(
                List(
                  IdenticalValue("a"),
                  IdenticalValue("b"),
                  IdenticalValue("c"),
                  IdenticalValue("d"),
                  DiffResultAdditionalChunk("e"),
                  DiffResultAdditionalChunk("f"),
                  DiffResultAdditionalChunk("g"),
                  DiffResultAdditionalChunk("h")
                )
              )
            )
          )
        )
      )
    }

    "multiple changes - quite similar" in {
      val left = "person"
      val right = "parzon"
      diffForString(left, right) shouldBe DiffResultString(
        List(
          DiffResultStringLine(
            List(
              DiffResultStringWord(
                List(
                  IdenticalValue("p"),
                  DiffResultChunk("e", "a"),
                  IdenticalValue("r"),
                  DiffResultChunk("s", "z"),
                  IdenticalValue("o"),
                  IdenticalValue("n")
                )
              )
            )
          )
        )
      )
    }

    "multiple changes - quite different" in {
      val left = "person"
      val right = "larsum"
      diffForString(left, right) shouldBe DiffResultString(
        List(
          DiffResultStringLine(
            List(
              DiffResultValue("person", "larsum")
            )
          )
        )
      )
    }
  }

  "words" - {
    "identical" - {
      val left = "abc abc"
      val right = "abc abc"
      diffForString(left, right).isIdentical shouldBe true
    }
    "missing whitespace at the end" in {
      val left = "abc abc"
      val right = "abc abc "
      diffForString(left, right) shouldBe DiffResultString(
        List(
          DiffResultStringLine(
            List(IdenticalValue("abc"), IdenticalValue(" "), IdenticalValue("abc"), DiffResultMissingChunk(" "))
          )
        )
      )
    }
    "missing word at the end of the line" in {
      val left = "alice bob"
      val right = "alice bob mark"
      val result = diffForString(left, right)
      result shouldBe DiffResultString(
        List(
          DiffResultStringLine(
            List(
              IdenticalValue("alice"),
              IdenticalValue(" "),
              IdenticalValue("bob"),
              DiffResultMissingChunk(" "),
              DiffResultMissingChunk("mark")
            )
          )
        )
      )
    }

    "missing word at the beginning of the line" in {
      val left = "alice bob"
      val right = "mark alice bob"
      diffForString(left, right) shouldBe DiffResultString(
        List(
          DiffResultStringLine(
            List(
              DiffResultMissingChunk("mark"),
              DiffResultMissingChunk(" "),
              IdenticalValue("alice"),
              IdenticalValue(" "),
              IdenticalValue("bob")
            )
          )
        )
      )
    }

    "missing word in the middle of the line" in {
      val left = "alice bob"
      val right = "alice mark bob"
      diffForString(left, right) shouldBe DiffResultString(
        List(
          DiffResultStringLine(
            List(
              IdenticalValue("alice"),
              IdenticalValue(" "),
              DiffResultMissingChunk("mark"),
              DiffResultMissingChunk(" "),
              IdenticalValue("bob")
            )
          )
        )
      )
    }

    "wrong word in the middle" in {
      val left = "alice lark bob"
      val right = "alice mark bob"
      diffForString(left, right) shouldBe DiffResultString(
        List(
          DiffResultStringLine(
            List(
              IdenticalValue("alice"),
              IdenticalValue(" "),
              DiffResultStringWord(
                List(
                  DiffResultChunk("l", "m"),
                  IdenticalValue("a"),
                  IdenticalValue("r"),
                  IdenticalValue("k")
                )
              ),
              IdenticalValue(" "),
              IdenticalValue("bob")
            )
          )
        )
      )
    }
  }
  "lines" - {
    "identical" - {
      val left = "abc\nabc"
      val right = "abc\nabc"
      diffForString(left, right).isIdentical shouldBe true
    }
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
}
