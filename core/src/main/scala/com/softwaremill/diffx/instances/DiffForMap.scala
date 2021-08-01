package com.softwaremill.diffx.instances

import com.softwaremill.diffx.ObjectMatcher.MapEntry
import com.softwaremill.diffx._
import com.softwaremill.diffx.instances.internal.{IndexedEntry, MatchResult}

import scala.annotation.tailrec

private[diffx] class DiffForMap[K, V, C[KK, VV] <: scala.collection.Map[KK, VV]](
    matcher: ObjectMatcher[MapEntry[K, V]],
    diffKey: Diff[K],
    diffValue: Diff[V]
) extends Diff[C[K, V]] {
  override def apply(
      left: C[K, V],
      right: C[K, V],
      context: DiffContext
  ): DiffResult = nullGuard(left, right) { (left, right) =>
    val adjustedMatcher = context.getMatcherOverride[MapEntry[K, V]].getOrElse(matcher)
    val matches = matchPairs(
      left.toList.zipWithIndex.map { case ((k, v), i) => IndexedEntry(i, MapEntry(k, v)) },
      right.toList.zipWithIndex.map { case ((k, v), i) => IndexedEntry(i, MapEntry(k, v)) },
      adjustedMatcher,
      List.empty,
      context
    )
    val diffs = matches.map {
      case MatchResult.UnmatchedLeft(entry)  => DiffResultAdditional(entry.key) -> DiffResultAdditional(entry.value)
      case MatchResult.UnmatchedRight(entry) => DiffResultMissing(entry.key) -> DiffResultMissing(entry.value)
      case MatchResult.Matched(lEntry, rEntry) =>
        diffKey(lEntry.key, rEntry.key, context) -> diffValue(lEntry.value, rEntry.value, context)
    }
    DiffResultMap(diffs.toMap)
  }

  @tailrec
  private def matchPairs(
      left: List[IndexedEntry[MapEntry[K, V]]],
      right: List[IndexedEntry[MapEntry[K, V]]],
      matcher: ObjectMatcher[MapEntry[K, V]],
      matched: List[MatchResult[MapEntry[K, V]]],
      context: DiffContext
  ): List[MatchResult[MapEntry[K, V]]] = {
    right match {
      case rHead :: rTail =>
        val maybeMatched = left
          .map { l =>
            val isSame = matcher.isSameObject(rHead.value, l.value)
            val isIdentical = diffKey.apply(l.value.key, rHead.value.key, context).isIdentical
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
              matched :+ MatchResult.Matched(lm.value, rm.value),
              context
            )
          case None => matchPairs(left, rTail, matcher, matched :+ MatchResult.UnmatchedRight(rHead.value), context)
        }
      case Nil => matched ++ left.map(l => MatchResult.UnmatchedLeft(l.value))
    }
  }
}
