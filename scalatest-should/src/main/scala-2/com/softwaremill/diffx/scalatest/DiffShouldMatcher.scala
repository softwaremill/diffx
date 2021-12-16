package com.softwaremill.diffx.scalatest

import com.softwaremill.diffx.{ShowConfig, Diff}
import org.scalactic.{Prettifier, source}
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.{MatchResult, Matcher}

trait DiffShouldMatcher {
  implicit def convertToAnyShouldMatcher[T: Diff](
      any: T
  )(implicit pos: source.Position, prettifier: Prettifier, c: ShowConfig): DiffAnyShouldWrapper[T] =
    new DiffAnyShouldWrapper[T](any)

  final class DiffAnyShouldWrapper[T](val leftValue: T)(implicit
      val pos: source.Position,
      val prettifier: Prettifier,
      val c: ShowConfig,
      val d: Diff[T]
  ) extends Matchers {

    def shouldMatchTo(rightValue: T): Assertion = {
      leftValue should matchTo[T](rightValue)
    }

    private def matchTo[A: Diff](right: A): Matcher[A] = { left =>
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
}

object DiffShouldMatcher extends DiffShouldMatcher
