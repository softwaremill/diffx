package com.softwaremill.diffx

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

  implicit def diffForOption[T](implicit ddt: Derived[Diff[T]]): Derived[Diff[Option[T]]] =
    Derived((left: Option[T], right: Option[T], toIgnore: List[FieldPath]) => {
      (left, right) match {
        case (Some(l), Some(r)) => ddt.value.apply(l, r, toIgnore)
        case (None, None)       => Identical(None)
        case (l, r)             => DiffResultValue(l, r)
      }
    })

  implicit def diffForSet[T: ObjectMatcher, C[W] <: scala.collection.Set[W]](
      implicit ddt: Derived[Diff[T]]
  ): Derived[Diff[C[T]]] =
    new Derived((left: C[T], right: C[T], toIgnore: List[FieldPath]) => {
      val matcher = implicitly[ObjectMatcher[T]]
      val matchedInstances = left.flatMap(
        l =>
          right.collectFirst { case r if matcher.isSameObject(l, r) || ddt(l, r, toIgnore) == Identical(l) => l -> r }
      )
      val unMatchedLeftInstances = left.diff(matchedInstances.map(_._1))
      val unMatchedRightInstances = right.diff(matchedInstances.map(_._2))

      val leftDiffs = unMatchedLeftInstances
        .diff(unMatchedRightInstances)
        .map(DiffResultAdditional(_))
        .toList
      val rightDiffs = unMatchedRightInstances
        .diff(unMatchedLeftInstances)
        .map(DiffResultMissing(_))
        .toList
      val matchedDiffs = matchedInstances.map { case (l, r) => ddt(l, r, toIgnore) }
      val diffs = leftDiffs ++ rightDiffs ++ matchedDiffs
      if (diffs.forall(_.isInstanceOf[Identical[_]])) {
        Identical(left)
      } else {
        DiffResultSet(diffs)
      }
    })

  implicit def diffForIterable[T, C[W] <: Iterable[W]](
      implicit ddot: Derived[Diff[Option[T]]]
  ): Derived[Diff[C[T]]] =
    Derived((left: C[T], right: C[T], toIgnore: List[FieldPath]) => {
      val indexes = Range(0, Math.max(left.size, right.size))
      val leftAsMap = left.toList.lift
      val rightAsMap = right.toList.lift
      DiffResultObject(
        "List",
        indexes.map { index =>
          index.toString -> (ddot.value
            .apply(leftAsMap(index), rightAsMap(index), toIgnore) match {
            case DiffResultValue(Some(v), None) => DiffResultAdditional(v)
            case DiffResultValue(None, Some(v)) => DiffResultMissing(v)
            case d                              => d
          })
        }.toMap
      )
    })

  implicit def diffForMap[T, C[_, _] <: Map[_, _]](
      implicit ddot: Derived[Diff[Option[T]]]
  ): Derived[Diff[Map[String, T]]] =
    Derived((left: Map[String, T], right: Map[String, T], toIgnore: List[FieldPath]) => {
      val keySet = left.keySet ++ right.keySet
      val diffs = keySet.map { k =>
        k -> ddot.value.apply(left.get(k), right.get(k), toIgnore)
      }.toMap
      if (diffs.values.forall(_.isInstanceOf[Identical[_]])) {
        Identical(left)
      } else {
        DiffResultObject("Map", diffs)
      }
    })
}

object DiffInstances extends DiffInstances
