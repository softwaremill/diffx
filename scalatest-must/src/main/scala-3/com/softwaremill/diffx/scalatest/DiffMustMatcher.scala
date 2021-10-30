package com.softwaremill.diffx.scalatest

import com.softwaremill.diffx.{ConsoleColorConfig, Diff}
import org.scalactic.{Prettifier, source}
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.{MatchResult, Matcher}

trait DiffMustMatcher {

  extension [T](leftSideValue: T)(using pos: source.Position, prettifier: Prettifier, diff: Diff[T], c: ConsoleColorConfig) def mustMatchTo(rightValue: T): Assertion = {
    import Matchers.must
    leftSideValue must matchTo(rightValue)
  }

  private def matchTo[A: Diff](right: A)(implicit c: ConsoleColorConfig): Matcher[A] = { left =>
    val result = Diff[A].apply(left, right)
    if (!result.isIdentical) {
      val diff =
        result.show().split('\n').mkString(Console.RESET, s"${Console.RESET}\n${Console.RESET}", Console.RESET)
      MatchResult(matches = false, s"Matching error:\n$diff", "")
    } else {
      MatchResult(matches = true, "", "")
    }
  }
}

object DiffMustMatcher extends DiffMustMatcher
