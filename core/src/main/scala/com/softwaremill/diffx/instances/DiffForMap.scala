package com.softwaremill.diffx.instances

import com.softwaremill.diffx.Matching._
import com.softwaremill.diffx._

private[diffx] class DiffForMap[K, V, C[KK, VV] <: scala.collection.Map[KK, VV]](
    matcher: ObjectMatcher[K],
    diffKey: Diff[K],
    diffValue: Diff[Option[V]]
) extends Diff[C[K, V]] {
  override def apply(
      left: C[K, V],
      right: C[K, V],
      context: DiffContext
  ): DiffResult = nullGuard(left, right) { (left, right) =>
    val MatchingResults(unMatchedLeftKeys, unMatchedRightKeys, matchedKeys) =
      matching[K](left.keySet, right.keySet, matcher, diffKey, context)
    val leftDiffs = this.leftDiffs(left, unMatchedLeftKeys, unMatchedRightKeys)
    val rightDiffs = this.rightDiffs(right, unMatchedLeftKeys, unMatchedRightKeys)
    val matchedDiffs = this.matchedDiffs(matchedKeys, left, right, context)
    val diffs = leftDiffs ++ rightDiffs ++ matchedDiffs
    if (diffs.forall(p => p._1.isIdentical && p._2.isIdentical)) {
      Identical(left)
    } else {
      DiffResultMap(diffs.toMap)
    }
  }

  private def matchedDiffs(
      matchedKeys: scala.collection.Set[(K, K)],
      left: C[K, V],
      right: C[K, V],
      context: DiffContext
  ): List[(DiffResult, DiffResult)] = {
    matchedKeys.map { case (lKey, rKey) =>
      val result = diffKey.apply(lKey, rKey)
      result -> diffValue.apply(left.get(lKey), right.get(rKey), context)
    }.toList
  }

  private def rightDiffs(
      right: C[K, V],
      unMatchedLeftKeys: scala.collection.Set[K],
      unMatchedRightKeys: scala.collection.Set[K]
  ): List[(DiffResult, DiffResult)] = {
    unMatchedRightKeys
      .diff(unMatchedLeftKeys)
      .map(k => DiffResultMissing(k) -> DiffResultMissing(right(k)))
      .toList
  }

  private def leftDiffs(
      left: C[K, V],
      unMatchedLeftKeys: scala.collection.Set[K],
      unMatchedRightKeys: scala.collection.Set[K]
  ): List[(DiffResult, DiffResult)] = {
    unMatchedLeftKeys
      .diff(unMatchedRightKeys)
      .map(k => DiffResultAdditional(k) -> DiffResultAdditional(left(k)))
      .toList
  }
}
