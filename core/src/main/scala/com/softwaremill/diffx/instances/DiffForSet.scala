package com.softwaremill.diffx.instances

import com.softwaremill.diffx.Matching.{MatchingResults, matching}
import com.softwaremill.diffx._

private[diffx] class DiffForSet[T, C[W] <: scala.collection.Set[W]](dt: Diff[T], matcher: ObjectMatcher[T])
    extends Diff[C[T]] {
  override def apply(left: C[T], right: C[T], context: DiffContext): DiffResult = nullGuard(left, right) {
    (left, right) =>
      val adjustedMatcher = context.getMatcherOverride[T].getOrElse(matcher)
      val MatchingResults(unMatchedLeftInstances, unMatchedRightInstances, matchedInstances) =
        matching[T](left.toSet, right.toSet, adjustedMatcher, dt, context)
      val leftDiffs = unMatchedLeftInstances
        .diff(unMatchedRightInstances)
        .map(DiffResultAdditional(_))
        .toList
      val rightDiffs = unMatchedRightInstances
        .diff(unMatchedLeftInstances)
        .map(DiffResultMissing(_))
        .toList
      val matchedDiffs = matchedInstances.map { case (l, r) => dt(l, r, context) }.toList
      diffResultSet(left, leftDiffs, rightDiffs, matchedDiffs)
  }

  private def diffResultSet(
      left: C[T],
      leftDiffs: List[DiffResult],
      rightDiffs: List[DiffResult],
      matchedDiffs: List[DiffResult]
  ): DiffResult = {
    val diffs = leftDiffs ++ rightDiffs ++ matchedDiffs
    DiffResultSet(diffs)
  }
}
