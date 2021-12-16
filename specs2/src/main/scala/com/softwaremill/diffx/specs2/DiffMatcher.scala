package com.softwaremill.diffx.specs2

import com.softwaremill.diffx.{ShowConfig, Diff}
import org.specs2.matcher.{Expectable, MatchResult, Matcher}

trait DiffMatcher {
  def matchTo[A: Diff](left: A)(implicit c: ShowConfig): DiffForMatcher[A] = DiffForMatcher(left)

  case class DiffForMatcher[A: Diff](right: A) extends Matcher[A] {
    override def apply[S <: A](left: Expectable[S]): MatchResult[S] = {
      val diff = Diff[A]
      result(
        test = {
          diff.apply(left.value, right).isIdentical
        },
        okMessage = "",
        koMessage = {
          val diffResult = diff.apply(left.value, right)
          if (!diffResult.isIdentical) {
            diffResult.show()
          } else {
            ""
          }
        },
        left
      )
    }
  }
}

object DiffMatcher extends DiffMatcher
