package com.softwaremill.diffx.scalatest

import com.softwaremill.diffx.{ConsoleColorConfig, Diff}
import org.scalactic.{Prettifier, source}
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.{MatchResult, Matcher}

trait DiffMustMatcher {

  implicit def convertToAnyMustMatcher[T: Diff](
      any: T
  )(implicit pos: source.Position, prettifier: Prettifier, consoleColorConfig: ConsoleColorConfig): AnyMustWrapper[T] =
    new AnyMustWrapper[T](any, pos, prettifier, consoleColorConfig, Diff[T])

  final class AnyMustWrapper[T](
      val leftValue: T,
      val pos: source.Position,
      val prettifier: Prettifier,
      val consoleColorConfig: ConsoleColorConfig,
      val diff: Diff[T]
  ) extends Matchers {

    def mustMatchTo(rightValue: T): Assertion = {
      Matchers
        .convertToAnyMustWrapper[T](leftValue)(pos, prettifier)
        .must(matchTo[T](rightValue)(diff, consoleColorConfig))
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
}

object DiffMustMatcher extends DiffMustMatcher
