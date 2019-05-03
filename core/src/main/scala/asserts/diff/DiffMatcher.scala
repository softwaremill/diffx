package asserts.diff

import java.time.Instant

import ai.x.diff.{DiffShow, Different, Error, Identical}
import org.scalatest.matchers.{MatchResult, Matcher}

trait DiffMatcher {
  def matchTo[A: DiffShow](left: A): UserMatcher[A] =
    UserMatcher.apply(left)

  case class UserMatcher[A: DiffShow](right: A) extends Matcher[A] {
    override def apply(left: A): MatchResult = DiffShow.diff[A](left, right) match {
      case _: Identical => MatchResult(matches = true, "Users does not match", "")
      case c: Different =>
        println(Console.RESET + Console.BLUE + c.string)
//        val value = s"$left was not equal to ${List.fill(100) { right }.mkString("\n")}"
        MatchResult(matches = false, right.toString, "a co to?")
      case _: Error => MatchResult(matches = false, "Error while comparing", "a co to?")
    }
  }

  implicit def diffForInstant: DiffShow[Instant] = DiffShow.primitive(_.toString)
}
