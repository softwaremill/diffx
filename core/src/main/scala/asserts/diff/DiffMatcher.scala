package asserts.diff

import org.scalatest.matchers.{MatchResult, Matcher}

trait DiffMatcher extends DiffForInstances {
  def matchTo[A: DiffFor](left: A): UserMatcher[A] =
    UserMatcher.apply(left)

  case class UserMatcher[A: DiffFor](right: A) extends Matcher[A] {
    override def apply(left: A): MatchResult = implicitly[DiffFor[A]].diff(left, right) match {
      case c: DiffResultDifferent =>
        println(c.show)
        MatchResult(matches = false, "Matching error", "a co to?")
      case _ => MatchResult(matches = true, "", "")
    }
  }
}
