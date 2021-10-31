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
    new AnyMustWrapper[T](any)

  final class AnyMustWrapper[T](val leftValue: T)(implicit
      val pos: source.Position,
      val prettifier: Prettifier,
      val consoleColorConfig: ConsoleColorConfig,
      val diff: Diff[T]
  ) extends Matchers {

    def mustMatchTo(rightValue: T): Assertion = {
      leftValue must matchTo[T](rightValue)
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

object DiffMustMatcher extends DiffMustMatcher
