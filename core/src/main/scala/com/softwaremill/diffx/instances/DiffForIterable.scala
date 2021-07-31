package com.softwaremill.diffx.instances

import com.softwaremill.diffx.ObjectMatcher.{IterableEntry, MapEntry}
import com.softwaremill.diffx._

import scala.annotation.tailrec
import scala.collection.immutable.ListMap

private[diffx] class DiffForIterable[T, C[W] <: Iterable[W]](
    dt: Diff[T],
    matcher: ObjectMatcher[IterableEntry[T]]
) extends Diff[C[T]] {
  override def apply(left: C[T], right: C[T], context: DiffContext): DiffResult = nullGuard(left, right) {
    (left, right) =>
      val adjustedMatcher = context.getMatcherOverride[IterableEntry[T]].getOrElse(matcher)
      val leftWithIndex = left.zipWithIndex.map { case (lv, i) => MapEntry(i, lv) }.toList
      val rightWithIndex = right.zipWithIndex.map { case (rv, i) => MapEntry(i, rv) }.toList

      val matches = matchPairs(leftWithIndex, rightWithIndex, adjustedMatcher, List.empty, context)
      implicit val iterableEntryOrdering: Ordering[IterableEntry[T]] = Ordering.by(_.key)
      implicit val diffResultOrdering: Ordering[MatchResult[T]] = new Ordering[MatchResult[T]] {
        override def compare(x: MatchResult[T], y: MatchResult[T]): Int = {
          (x, y) match {
            case (ur: MatchResult.UnmatchedRight[T], m: MatchResult.Matched[T]) =>
              iterableEntryOrdering.compare(ur.v, m.r)
            case (_: MatchResult.UnmatchedRight[_], _: MatchResult.UnmatchedLeft[_]) => 1
            case (ur: MatchResult.UnmatchedRight[T], ur2: MatchResult.UnmatchedRight[T]) =>
              iterableEntryOrdering.compare(ur.v, ur2.v)
            case (ur: MatchResult.UnmatchedLeft[T], m: MatchResult.Matched[T]) =>
              iterableEntryOrdering.compare(ur.v, m.l)
            case (_: MatchResult.UnmatchedLeft[_], _: MatchResult.UnmatchedRight[_]) => -1
            case (ur: MatchResult.UnmatchedLeft[T], ur2: MatchResult.UnmatchedLeft[T]) =>
              iterableEntryOrdering.compare(ur.v, ur2.v)
            case (m1: MatchResult.Matched[T], m2: MatchResult.Matched[T]) =>
              Ordering.by[MatchResult.Matched[T], (IterableEntry[T], IterableEntry[T])](m => (m.r, m.l)).compare(m1, m2)
            case (m: MatchResult.Matched[T], ur: MatchResult.UnmatchedRight[T]) =>
              iterableEntryOrdering.compare(m.r, ur.v)
            case (m: MatchResult.Matched[T], ul: MatchResult.UnmatchedLeft[T]) =>
              iterableEntryOrdering.compare(m.l, ul.v)
          }
        }
      }
      val sortedDiffs = matches.sorted.map {
        case MatchResult.UnmatchedLeft(v)  => DiffResultAdditional(v.value)
        case MatchResult.UnmatchedRight(v) => DiffResultMissing(v.value)
        case MatchResult.Matched(l, r)     => dt.apply(l.value, r.value, context)
      }
      val reindexed = sortedDiffs.zipWithIndex.map(_.swap)
      val diffs = ListMap(reindexed.map { case (k, v) => k.toString -> v }: _*)
      DiffResultObject("List", diffs)
  }

  @tailrec
  private def matchPairs(
      left: List[IterableEntry[T]],
      right: List[IterableEntry[T]],
      matcher: ObjectMatcher[IterableEntry[T]],
      matched: List[MatchResult[T]],
      context: DiffContext
  ): List[MatchResult[T]] = {
    right match {
      case ::(rHead, tailRight) =>
        val maybeMatched = left
          .collect { case l if matcher.isSameObject(rHead, l) => l -> rHead }
          .sortBy { case (l, r) => !dt.apply(l.value, r.value, context).isIdentical }
          .headOption
        maybeMatched match {
          case Some((lm, rm)) =>
            matchPairs(
              left.filterNot(l => l.key == lm.key),
              tailRight,
              matcher,
              matched :+ MatchResult.Matched(lm, rm),
              context
            )
          case None => matchPairs(left, tailRight, matcher, matched :+ MatchResult.UnmatchedRight(rHead), context)
        }
      case Nil => matched ++ left.map(l => MatchResult.UnmatchedLeft(l))
    }
  }
}

sealed trait MatchResult[T]

object MatchResult {
  case class UnmatchedLeft[T](v: IterableEntry[T]) extends MatchResult[T]
  case class UnmatchedRight[T](v: IterableEntry[T]) extends MatchResult[T]
  case class Matched[T](l: IterableEntry[T], r: IterableEntry[T]) extends MatchResult[T]
}
