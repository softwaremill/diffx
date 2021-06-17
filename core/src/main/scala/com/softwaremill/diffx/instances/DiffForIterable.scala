package com.softwaremill.diffx.instances

import com.softwaremill.diffx.Matching.{MatchingResults, matching}
import com.softwaremill.diffx._

import scala.collection.immutable.{ListMap, ListSet}

private[diffx] class DiffForIterable[T, C[W] <: Iterable[W]](
    dt: Diff[T],
    matcher: ObjectMatcher[(Int, T)]
) extends Diff[C[T]] {
  override def apply(left: C[T], right: C[T], context: DiffContext): DiffResult = nullGuard(left, right) {
    (left, right) =>
      val keys = Range(0, Math.max(left.size, right.size))

      val leftAsMap = left.toList.lift
      val rightAsMap = right.toList.lift
      val leftv2 = ListSet(keys.map(i => i -> leftAsMap(i)): _*).collect { case (k, Some(v)) => k -> v }
      val rightv2 = ListSet(keys.map(i => i -> rightAsMap(i)): _*).collect { case (k, Some(v)) => k -> v }

      val MatchingResults(unMatchedLeftInstances, unMatchedRightInstances, matchedInstances) =
        matching(leftv2, rightv2, matcher, dt.contramap[(Int, T)](_._2), context)
      val leftDiffs = unMatchedLeftInstances
        .diff(unMatchedRightInstances)
        .collectFirst { case (k, v) => k -> DiffResultAdditional(v) }
        .toList
      val rightDiffs = unMatchedRightInstances
        .diff(unMatchedLeftInstances)
        .collectFirst { case (k, v) => k -> DiffResultMissing(v) }
        .toList
      val matchedDiffs = matchedInstances.map { case (l, r) => l._1 -> dt(l._2, r._2, context) }.toList

      val diffs = ListMap((matchedDiffs ++ leftDiffs ++ rightDiffs).map { case (k, v) => k.toString -> v }: _*)
      if (diffs.forall { case (_, v) => v.isIdentical }) {
        Identical(left)
      } else {
        DiffResultObject("List", diffs)
      }
  }
}
