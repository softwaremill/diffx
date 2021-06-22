package com.softwaremill.diffx.instances

import com.softwaremill.diffx.Matching._
import com.softwaremill.diffx._

private[diffx] class DiffForMap[K, V, C[KK, VV] <: scala.collection.Map[KK, VV]](
    matcher: ObjectMatcher[(K, V)],
    diffKey: Diff[K],
    diffValue: Diff[V]
) extends Diff[C[K, V]] {
  override def apply(
      left: C[K, V],
      right: C[K, V],
      context: DiffContext
  ): DiffResult = nullGuard(left, right) { (left, right) =>
    val adjustedMatcher = context.getMatcherOverride[(K, V)].getOrElse(matcher)
    val MatchingResults(unMatchedLeftKeys, unMatchedRightKeys, matchedKeys) =
      matching(left.toSet, right.toSet, adjustedMatcher, diffKey.contramap[(K, V)](_._1), context)

    val leftDiffs = unMatchedLeftKeys
      .diff(unMatchedRightKeys)
      .collectFirst { case (k, v) => DiffResultAdditional(k) -> DiffResultAdditional(v) }
      .toList
    val rightDiffs = unMatchedRightKeys
      .diff(unMatchedLeftKeys)
      .collectFirst { case (k, v) => DiffResultMissing(k) -> DiffResultMissing(v) }
      .toList
    val matchedDiffs = matchedKeys.map { case (l, r) => diffKey(l._1, r._1) -> diffValue(l._2, r._2, context) }.toList

    val diffs = leftDiffs ++ rightDiffs ++ matchedDiffs
    DiffResultMap(diffs.toMap)
  }
}
