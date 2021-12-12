package com.softwaremill.diffx.scalatest

import com.softwaremill.diffx.{ShowConfig, Diff}
import org.scalatest.matchers.{MatchResult, Matcher}

@deprecated("Use DiffShouldMatcher or DiffMustMatcher instead")
trait DiffMatcher {
  def matchTo[A: Diff](right: A)(implicit c: ShowConfig): Matcher[A] = { left =>
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

@deprecated("Use DiffShouldMatcher or DiffMustMatcher instead")
object DiffMatcher extends DiffMatcher
