package com.softwaremill.diffx.instances

import com.softwaremill.diffx.ObjectMatcher.SetEntry
import com.softwaremill.diffx._
import com.softwaremill.diffx.instances.internal.{IndexedEntry, MatchResult}

import scala.annotation.tailrec

private[diffx] class DiffForSet[C[_], T](
    dt: Diff[T],
    matcher: ObjectMatcher[SetEntry[T]],
    setLike: SetLike[C],
    typename: String = "Set"
) extends Diff[C[T]] {
  override def apply(left: C[T], right: C[T], context: DiffContext): DiffResult = nullGuard(left, right) {
    (left, right) =>
      val adjustedMatcher = context.getMatcherOverride[SetEntry[T]].getOrElse(matcher)
      val matches = matchPairs(
        setLike.asSet(left).toList.zipWithIndex.map(p => IndexedEntry(p._2, SetEntry(p._1))),
        setLike.asSet(right).toList.zipWithIndex.map(p => IndexedEntry(p._2, SetEntry(p._1))),
        adjustedMatcher,
        List.empty,
        context
      )
      val diffs = matches.map {
        case MatchResult.UnmatchedLeft(v)  => DiffResultAdditional(v)
        case MatchResult.UnmatchedRight(v) => DiffResultMissing(v)
        case MatchResult.Matched(l, r)     => dt.apply(l, r, context)
      }
      DiffResultSet(typename, diffs.toSet)
  }
  @tailrec
  private def matchPairs(
      left: List[IndexedEntry[SetEntry[T]]],
      right: List[IndexedEntry[SetEntry[T]]],
      matcher: ObjectMatcher[SetEntry[T]],
      matched: List[MatchResult[T]],
      context: DiffContext
  ): List[MatchResult[T]] = {
    right match {
      case rHead :: rTail =>
        val maybeMatched = left
          .map { l =>
            val isSame = matcher.isSameObject(rHead.value, l.value)
            val isIdentical = dt.apply(l.value.t, rHead.value.t, context).isIdentical
            (l -> rHead, isSame, isIdentical)
          }
          .filter { case (_, isSame, isIdentical) => isSame || isIdentical }
          .sortBy { case (_, _, isIdentical) => !isIdentical }
          .map { case (lr, _, _) => lr }
          .headOption
        maybeMatched match {
          case Some((lm, rm)) =>
            matchPairs(
              left.filterNot(l => l.index == lm.index),
              rTail,
              matcher,
              matched :+ MatchResult.Matched(lm.value.t, rm.value.t),
              context
            )
          case None => matchPairs(left, rTail, matcher, matched :+ MatchResult.UnmatchedRight(rHead.value.t), context)
        }
      case Nil => matched ++ left.map(l => MatchResult.UnmatchedLeft(l.value.t))
    }
  }
}
