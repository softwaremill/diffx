package com.softwaremill.diffx.scalatest

import com.softwaremill.diffx.{ConsoleColorConfig, Diff}
import org.scalatest.matchers.{MatchResult, Matcher}

trait DiffMatcher {
  def matchTo[A: Diff](right: A)(implicit c: ConsoleColorConfig): Matcher[A] = { left =>
    val result = Diff[A].apply(left, right)
    if (!result.isIdentical) {
      val diff = result.show().split('\n').mkString(Console.RESET, s"${Console.RESET}\n${Console.RESET}", Console.RESET)
      MatchResult(matches = false, s"Matching error:\n$diff", "")
    } else {
      MatchResult(matches = true, "", "")
    }
  }
}

object DiffMatcher extends DiffMatcher
