package com.softwaremill.diffx.instances

import com.softwaremill.diffx.Matching.{MatchingResults, matching}
import com.softwaremill.diffx.ObjectMatcher.{IterableEntry, MapEntry}
import com.softwaremill.diffx._

import scala.collection.immutable.{ListMap, ListSet}

private[diffx] class DiffForIterable[T, C[W] <: Iterable[W]](
    dt: Diff[T],
    matcher: ObjectMatcher[IterableEntry[T]]
) extends Diff[C[T]] {
  override def apply(left: C[T], right: C[T], context: DiffContext): DiffResult = nullGuard(left, right) {
    (left, right) =>
      val keys = Range(0, Math.max(left.size, right.size))

      val leftAsMap = left.toList.lift
      val rightAsMap = right.toList.lift
      val leftv2 = ListSet(keys.map(i => i -> leftAsMap(i)): _*).collect { case (k, Some(v)) => MapEntry(k, v) }
      val rightv2 = ListSet(keys.map(i => i -> rightAsMap(i)): _*).collect { case (k, Some(v)) => MapEntry(k, v) }

      val adjustedMatcher = context.getMatcherOverride[IterableEntry[T]].getOrElse(matcher)
      val MatchingResults(unMatchedLeftInstances, unMatchedRightInstances, matchedInstances) =
        matching(leftv2, rightv2, adjustedMatcher)
      val leftDiffs = unMatchedLeftInstances
        .diff(unMatchedRightInstances)
        .collectFirst { case MapEntry(k, v) => k -> DiffResultAdditional(v) }
        .toList
      val rightDiffs = unMatchedRightInstances
        .diff(unMatchedLeftInstances)
        .collectFirst { case MapEntry(k, v) => k -> DiffResultMissing(v) }
        .toList
      val matchedDiffs = matchedInstances.map { case (l, r) => l.key -> dt(l.value, r.value, context) }.toList

      val diffs = ListMap((matchedDiffs ++ leftDiffs ++ rightDiffs).map { case (k, v) => k.toString -> v }: _*)
      DiffResultObject("List", diffs)
  }
}
