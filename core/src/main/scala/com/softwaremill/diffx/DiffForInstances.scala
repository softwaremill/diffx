package com.softwaremill.diffx

trait DiffForInstances extends DiffForMagnoliaDerivation with DiffForMacroIgnoreExt {

  implicit def diffForNumeric[T: Numeric]: Derived[DiffFor[T]] =
    Derived((left: T, right: T, _: List[List[String]]) => {
      val numeric = implicitly[Numeric[T]]
      if (!numeric.equiv(left, right)) {
        DiffResultValue(left, right)
      } else {
        Identical(left)
      }
    })

  implicit def diffForOption[T](implicit ddt: Derived[DiffFor[T]]): Derived[DiffFor[Option[T]]] =
    Derived((left: Option[T], right: Option[T], toIgnore: List[List[String]]) => {
      (left, right) match {
        case (Some(l), Some(r)) => ddt.value.apply(l, r, toIgnore)
        case (None, None)       => Identical(None)
        case (l, r)             => DiffResultValue(l, r)
      }
    })

  implicit def diffForSet[T: EntityMatcher, C[W] <: scala.collection.Set[W]](
      implicit ddt: Derived[DiffFor[T]]
  ): Derived[DiffFor[C[T]]] =
    new Derived((left: C[T], right: C[T], toIgnore: List[List[String]]) => {
      val matcher = implicitly[EntityMatcher[T]]
      val matchedInstances = left.flatMap(l => right.collectFirst { case r if matcher.isSameEntity(l, r) => l -> r })
      val unMatchedLeftInstances = left.diff(matchedInstances.map(_._1))
      val unMatchedRightInstances = right.diff(matchedInstances.map(_._2))
      val differ = ddt.value

      val leftDiffs = unMatchedLeftInstances
        .diff(unMatchedRightInstances)
        .map(DiffResultAdditional(_))
        .toList
      val rightDiffs = unMatchedRightInstances
        .diff(unMatchedLeftInstances)
        .map(DiffResultMissing(_))
        .toList
      val matchedDiffs = matchedInstances.map { case (l, r) => differ(l, r, toIgnore) }
      val diffs = leftDiffs ++ rightDiffs ++ matchedDiffs
      if (diffs.isEmpty) {
        Identical(left)
      } else {
        DiffResultSet(diffs)
      }
    })

  implicit def diffForIterable[T, C[W] <: Iterable[W]](
      implicit ddot: Derived[DiffFor[Option[T]]]
  ): Derived[DiffFor[C[T]]] =
    Derived((left: C[T], right: C[T], toIgnore: List[List[String]]) => {
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
      implicit ddot: Derived[DiffFor[Option[T]]]
  ): Derived[DiffFor[Map[String, T]]] =
    Derived((left: Map[String, T], right: Map[String, T], toIgnore: List[List[String]]) => {
      val keySet = left.keySet ++ right.keySet
      DiffResultObject("Map", keySet.map { k =>
        k -> ddot.value.apply(left.get(k), right.get(k), toIgnore)
      }.toMap)
    })
}

object DiffForInstances extends DiffForInstances
