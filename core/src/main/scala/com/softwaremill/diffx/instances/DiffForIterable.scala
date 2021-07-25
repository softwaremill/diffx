package com.softwaremill.diffx.instances

import com.softwaremill.diffx.Matching.MatchingResults
import com.softwaremill.diffx.ObjectMatcher.{IterableEntry, MapEntry}
import com.softwaremill.diffx._

import scala.annotation.tailrec
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
      val lEntries = keys.map(i => i -> leftAsMap(i)).collect { case (k, Some(v)) => MapEntry(k, v) }.toList
      val rEntries = keys.map(i => i -> rightAsMap(i)).collect { case (k, Some(v)) => MapEntry(k, v) }.toList

      val adjustedMatcher = context.getMatcherOverride[IterableEntry[T]].getOrElse(matcher)
      val MatchingResults(unMatchedLeftInstances, unMatchedRightInstances, matchedInstances) =
        matchPairs(lEntries, rEntries, adjustedMatcher)
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

  private def matchPairs(
      left: List[IterableEntry[T]],
      right: List[IterableEntry[T]],
      matcher: ObjectMatcher[IterableEntry[T]]
  ): MatchingResults[IterableEntry[T]] = {
    @tailrec
    def loop(
        left: List[IterableEntry[T]],
        right: List[IterableEntry[T]],
        matches: List[(IterableEntry[T], IterableEntry[T])],
        leftUnmatched: List[IterableEntry[T]]
    ): MatchingResults[IterableEntry[T]] = {
      left match {
        case lHead :: tail =>
          val maybeMatch = right.collectFirst {
            case r if matcher.isSameObject(lHead, r) => lHead -> r
          }
          maybeMatch match {
            case Some(m @ (_, rm)) =>
              loop(tail, right.filterNot(r => r.key == rm.key), matches :+ m, leftUnmatched)
            case None => loop(tail, right, matches, leftUnmatched :+ lHead)
          }
        case Nil => MatchingResults(ListSet(leftUnmatched: _*), ListSet(right: _*), ListSet(matches: _*))
      }
    }
    loop(left, right, List.empty, List.empty)
  }
}
