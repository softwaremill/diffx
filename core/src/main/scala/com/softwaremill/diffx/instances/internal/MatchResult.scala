package com.softwaremill.diffx.instances.internal

private[instances] sealed trait MatchResult[T]

private[instances] object MatchResult {
  case class UnmatchedLeft[T](v: T) extends MatchResult[T]
  case class UnmatchedRight[T](v: T) extends MatchResult[T]
  case class Matched[T](l: T, r: T) extends MatchResult[T]
}
