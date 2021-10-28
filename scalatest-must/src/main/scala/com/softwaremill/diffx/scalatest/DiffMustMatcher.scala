package com.softwaremill.diffx.scalatest

import com.softwaremill.diffx.{ConsoleColorConfig, Diff}
import org.scalactic.{Prettifier, source}
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.{MatchResult, Matcher}

trait DiffMustMatcher {

  def matchTo[A](right: A): A = right

  implicit def convertToMustWrapper[T: Diff](
      any: T
  )(implicit pos: source.Position, prettifier: Prettifier): AnyMustWrapper[T] =
    new AnyMustWrapper[T](any, pos, prettifier)

  class AnyMustWrapper[T: Diff](
      val leftValue: T,
      val pos: source.Position,
      val prettifier: Prettifier
  ) extends Matchers {

    def must(rightValue: T)(implicit c: ConsoleColorConfig): Assertion = {
      Matchers.convertToAnyMustWrapper(leftValue)(pos, prettifier).must(matchTo(rightValue))
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
