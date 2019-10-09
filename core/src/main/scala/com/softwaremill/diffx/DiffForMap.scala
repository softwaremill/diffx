package com.softwaremill.diffx
import acyclic.skipped

import com.softwaremill.diffx.Matching._

private[diffx] class DiffForMap[K, V](matcher: ObjectMatcher[K], diffKey: Diff[K], diffValue: Diff[Option[V]])
    extends Diff[Map[K, V]] {
  override def apply(
      left: Map[K, V],
      right: Map[K, V],
      toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
  ): DiffResult = {
    val MatchingResults(unMatchedLeftKeys, unMatchedRightKeys, matchedKeys) =
      matching[K](left.keySet, right.keySet, matcher, diffKey, toIgnore)
    val leftDiffs = this.leftDiffs(left, unMatchedLeftKeys, unMatchedRightKeys)
    val rightDiffs = this.rightDiffs(right, unMatchedLeftKeys, unMatchedRightKeys)
    val matchedDiffs = this.matchedDiffs(matchedKeys, left, right, toIgnore)
    val diffs = leftDiffs ++ rightDiffs ++ matchedDiffs
    if (diffs.forall(p => p._1.isIdentical && p._2.isIdentical)) {
      Identical(left)
    } else {
      DiffResultMap(diffs.toMap)
    }
  }

  private def matchedDiffs(
      matchedKeys: Set[(K, K)],
      left: Map[K, V],
      right: Map[K, V],
      toIgnore: List[FieldPath]
  ): List[(DiffResult, DiffResult)] = {
    matchedKeys.map {
      case (lKey, rKey) =>
        val result = diffKey.apply(lKey, rKey)
        result -> diffValue.apply(left.get(lKey), right.get(rKey), toIgnore)
    }.toList
  }

  private def rightDiffs(
      right: Map[K, V],
      unMatchedLeftKeys: Set[K],
      unMatchedRightKeys: Set[K]
  ): List[(DiffResult, DiffResult)] = {
    unMatchedRightKeys
      .diff(unMatchedLeftKeys)
      .map(k => DiffResultMissing(k) -> DiffResultMissing(right(k)))
      .toList
  }

  private def leftDiffs(
      left: Map[K, V],
      unMatchedLeftKeys: Set[K],
      unMatchedRightKeys: Set[K]
  ): List[(DiffResult, DiffResult)] = {
    unMatchedLeftKeys
      .diff(unMatchedRightKeys)
      .map(k => DiffResultAdditional(k) -> DiffResultAdditional(left(k)))
      .toList
  }
}
