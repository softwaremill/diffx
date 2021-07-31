package com.softwaremill.diffx.instances

import com.softwaremill.diffx._

import scala.annotation.tailrec

private[diffx] class DiffForSet[T, C[W] <: scala.collection.Set[W]](dt: Diff[T], matcher: ObjectMatcher[T])
    extends Diff[C[T]] {
  override def apply(left: C[T], right: C[T], context: DiffContext): DiffResult = nullGuard(left, right) {
    (left, right) =>
      val adjustedMatcher = context.getMatcherOverride[T].getOrElse(matcher)
      val matches = matchPairs(
        left.toList.zipWithIndex.map(p => SetEntry(p._2, p._1)),
        right.toList.zipWithIndex.map(p => SetEntry(p._2, p._1)),
        adjustedMatcher,
        List.empty,
        context
      )
      val diffs = matches.map {
        case MatchResult.UnmatchedLeft(v)  => DiffResultAdditional(v)
        case MatchResult.UnmatchedRight(v) => DiffResultMissing(v)
        case MatchResult.Matched(l, r)     => dt.apply(l, r, context)
      }
      DiffResultSet(diffs.toSet)
  }
  @tailrec
  private def matchPairs(
      left: List[SetEntry[T]],
      right: List[SetEntry[T]],
      matcher: ObjectMatcher[T],
      matched: List[MatchResult[T]],
      context: DiffContext
  ): List[MatchResult[T]] = {
    right match {
      case rHead :: rTail =>
        val maybeMatched = left
          .collect {
            case l
                if matcher.isSameObject(rHead.value, l.value) || dt.apply(l.value, rHead.value, context).isIdentical =>
              l -> rHead
          }
          .sortBy { case (l, r) => !dt.apply(l.value, r.value, context).isIdentical }
          .headOption
        maybeMatched match {
          case Some((lm, rm)) =>
            matchPairs(
              left.filterNot(l => l.index == lm.index),
              rTail,
              matcher,
              matched :+ MatchResult.Matched(lm.value, rm.value),
              context
            )
          case None => matchPairs(left, rTail, matcher, matched :+ MatchResult.UnmatchedRight(rHead.value), context)
        }
      case Nil => matched ++ left.map(l => MatchResult.UnmatchedLeft(l.value))
    }
  }
}

private case class SetEntry[T](index: Int, value: T)
