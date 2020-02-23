package com.softwaremill.diffx
import acyclic.skipped
import com.softwaremill.diffx.Matching._

import scala.collection.immutable.ListMap

trait DiffInstances extends DiffMagnoliaDerivation {
  implicit def diffForNumeric[T: Numeric]: Derived[Diff[T]] =
    Derived((left: T, right: T, _: List[FieldPath]) => {
      val numeric = implicitly[Numeric[T]]
      if (!numeric.equiv(left, right)) {
        DiffResultValue(left, right)
      } else {
        Identical(left)
      }
    })

  implicit def diffForOption[T](implicit ddt: Diff[T]): Derived[Diff[Option[T]]] =
    Derived((left: Option[T], right: Option[T], toIgnore: List[FieldPath]) => {
      (left, right) match {
        case (Some(l), Some(r)) => ddt.apply(l, r, toIgnore)
        case (None, None)       => Identical(None)
        case (l, r)             => DiffResultValue(l, r)
      }
    })

  implicit def diffForSet[T: ObjectMatcher, C[W] <: scala.collection.Set[W]](
      implicit ddt: Diff[T],
      matcher: ObjectMatcher[T]
  ): Derived[Diff[C[T]]] =
    Derived((left: C[T], right: C[T], toIgnore: List[FieldPath]) => {
      val MatchingResults(unMatchedLeftInstances, unMatchedRightInstances, matchedInstances) =
        matching[T](left.toSet, right.toSet, matcher, ddt, toIgnore)
      val leftDiffs = unMatchedLeftInstances
        .diff(unMatchedRightInstances)
        .map(DiffResultAdditional(_))
        .toList
      val rightDiffs = unMatchedRightInstances
        .diff(unMatchedLeftInstances)
        .map(DiffResultMissing(_))
        .toList
      val matchedDiffs = matchedInstances.map { case (l, r) => ddt(l, r, toIgnore) }.toList
      diffResultSet(left, leftDiffs, rightDiffs, matchedDiffs)
    })

  private def diffResultSet[T](
      left: T,
      leftDiffs: List[DiffResult],
      rightDiffs: List[DiffResult],
      matchedDiffs: List[DiffResult]
  ): DiffResult = {
    val diffs = leftDiffs ++ rightDiffs ++ matchedDiffs
    if (diffs.forall(_.isIdentical)) {
      Identical(left)
    } else {
      DiffResultSet(diffs)
    }
  }

  implicit def diffForIterable[T, C[W] <: Iterable[W]](
      implicit ddot: Diff[Option[T]]
  ): Derived[Diff[C[T]]] =
    Derived((left: C[T], right: C[T], toIgnore: List[FieldPath]) => {
      val indexes = Range(0, Math.max(left.size, right.size))
      val leftAsMap = left.toList.lift
      val rightAsMap = right.toList.lift
      val differences = ListMap(indexes.map { index =>
        index.toString -> (ddot.apply(leftAsMap(index), rightAsMap(index), toIgnore) match {
          case DiffResultValue(Some(v), None) => DiffResultAdditional(v)
          case DiffResultValue(None, Some(v)) => DiffResultMissing(v)
          case d                              => d
        })
      }: _*)

      if (differences.values.forall(_.isIdentical)) {
        Identical(left)
      } else {
        DiffResultObject(
          "List",
          differences
        )
      }
    })

  implicit def diffForMap[K, V, C[KK, VV] <: scala.collection.Map[KK, VV]](
      implicit ddot: Diff[Option[V]],
      ddk: Diff[K],
      matcher: ObjectMatcher[K]
  ): Derived[Diff[C[K, V]]] = Derived(new DiffForMap[K, V, C](matcher, ddk, ddot))

  implicit def diffForString: Derived[Diff[String]] = Derived(new DiffForString)
}
