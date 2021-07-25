package com.softwaremill.diffx.instances

import com.softwaremill.diffx.Matching._
import com.softwaremill.diffx.ObjectMatcher.MapEntry
import com.softwaremill.diffx._

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
    val MatchingResults(unMatchedLeftKeys, unMatchedRightKeys, matchedKeys) =
      matching(
        left.map { case (k, v) => MapEntry.apply(k, v) }.toSet,
        right.map { case (k, v) => MapEntry.apply(k, v) }.toSet,
        adjustedMatcher,
        diffKey.contramap[MapEntry[K, V]](_.key),
        context
      )

    val leftDiffs = unMatchedLeftKeys
      .diff(unMatchedRightKeys)
      .collectFirst { case MapEntry(k, v) => DiffResultAdditional(k) -> DiffResultAdditional(v) }
      .toList
    val rightDiffs = unMatchedRightKeys
      .diff(unMatchedLeftKeys)
      .collectFirst { case MapEntry(k, v) => DiffResultMissing(k) -> DiffResultMissing(v) }
      .toList
    val matchedDiffs = matchedKeys.map { case (l, r) =>
      diffKey(l.key, r.key) -> diffValue(l.value, r.value, context)
    }.toList

    val diffs = leftDiffs ++ rightDiffs ++ matchedDiffs
    DiffResultMap(diffs.toMap)
  }
}
