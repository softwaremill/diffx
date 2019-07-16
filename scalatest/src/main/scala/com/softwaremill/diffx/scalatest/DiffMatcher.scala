package com.softwaremill.diffx.scalatest

import com.softwaremill.diffx.{DiffFor, DiffForInstances, DiffResultDifferent}
import org.scalatest.matchers.{MatchResult, Matcher}

trait DiffMatcher extends DiffForInstances {
  def matchTo[A: DiffFor](left: A): DiffForMatcher[A] = DiffForMatcher(left)

  case class DiffForMatcher[A: DiffFor](right: A) extends Matcher[A] {
    override def apply(left: A): MatchResult = implicitly[DiffFor[A]].diff(left, right) match {
      case c: DiffResultDifferent =>
        println(c.show)
        MatchResult(matches = false, "Matching error", "a co to?")
      case _ => MatchResult(matches = true, "", "")
    }
  }

}

object DiffMatcher extends DiffMatcher
