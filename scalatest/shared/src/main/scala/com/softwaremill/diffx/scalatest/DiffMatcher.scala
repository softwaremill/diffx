package com.softwaremill.diffx.scalatest

import com.softwaremill.diffx.{ConsoleColorConfig, Diff, DiffResultDifferent}
import org.scalatest.matchers.{MatchResult, Matcher}

trait DiffMatcher {
  def matchTo[A: Diff](left: A)(implicit c: ConsoleColorConfig): DiffForMatcher[A] = new DiffForMatcher(left)

  class DiffForMatcher[A: Diff](right: A)(implicit c: ConsoleColorConfig) extends Matcher[A] {
    override def apply(left: A): MatchResult = Diff[A].apply(left, right) match {
      case c: DiffResultDifferent =>
        val diff = c.show.split('\n').mkString(Console.RESET, s"${Console.RESET}\n${Console.RESET}", Console.RESET)
        MatchResult(matches = false, s"Matching error:\n$diff", "")
      case _ => MatchResult(matches = true, "", "")
    }
  }
}

object DiffMatcher extends DiffMatcher
