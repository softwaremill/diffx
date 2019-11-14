package com.softwaremill.diffx.scalatest

import com.softwaremill.diffx.{Diff, DiffResultDifferent}
import org.scalatest.matchers.{MatchResult, Matcher}

trait DiffMatcher {
  def matchTo[A: Diff](left: A): DiffForMatcher[A] = DiffForMatcher(left)

  case class DiffForMatcher[A: Diff](right: A) extends Matcher[A] {
    override def apply(left: A): MatchResult = Diff[A].apply(left, right) match {
      case c: DiffResultDifferent =>
        println(c.show)
        MatchResult(matches = false, "Matching error", "a co to?")
      case _ => MatchResult(matches = true, "", "")
    }
  }
}

object DiffMatcher extends DiffMatcher
