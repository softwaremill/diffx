package com.softwaremill.diffx.scalatest

import com.softwaremill.diffx.{ShowConfig, Diff}
import org.scalactic.{Prettifier, source}
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.{MatchResult, Matcher}

trait DiffShouldMatcher {
  extension [T](
      leftSideValue: T
  )(using pos: source.Position, prettifier: Prettifier, diff: Diff[T], c: ShowConfig)
    def shouldMatchTo(rightValue: T): Assertion = {
      import Matchers.should
      leftSideValue should matchTo(rightValue)
    }

  private def matchTo[A: Diff](right: A)(implicit c: ShowConfig): Matcher[A] = { left =>
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

object DiffShouldMatcher extends DiffShouldMatcher
