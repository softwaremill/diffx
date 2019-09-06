package com.softwaremill.diffx

trait DiffForInstances extends DiffForMagnoliaDerivation {

  implicit def diffForInt: DiffFor[Int] = (left: Int, right: Int, toIgnore: List[List[String]]) => {
    if (left != right) {
      DiffResultValue(left, right)
    } else {
      Identical(left)
    }
  }

  implicit def diffForOption[T: DiffFor]: DiffFor[Option[T]] =
    (left: Option[T], right: Option[T], toIgnore: List[List[String]]) => {
      (left, right) match {
        case (Some(l), Some(r)) => implicitly[DiffFor[T]].apply(l, r, toIgnore)
        case (None, None)       => Identical(None)
        case (l, r)             => DiffResultValue(l, r)
      }
    }

  implicit def diffForSet[T: DiffFor: EntityMatcher, C[W] <: scala.collection.Set[W]]: DiffFor[C[T]] =
    (left: C[T], right: C[T], toIgnore: List[List[String]]) => {
      val matcher = implicitly[EntityMatcher[T]]
      val matchedInstances = left.flatMap(l => right.collectFirst { case r if matcher.isSameEntity(l, r) => l -> r })
      val unMatchedLeftInstances = left.diff(matchedInstances.map(_._1))
      val unMatchedRightInstances = right.diff(matchedInstances.map(_._2))
      val differ = implicitly[DiffFor[T]]

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
    }

  implicit def diffForIterable[T: DiffFor, C[W] <: Iterable[W]]: DiffFor[C[T]] =
    (left: C[T], right: C[T], toIgnore: List[List[String]]) => {
      val indexes = Range(0, Math.max(left.size, right.size))
      val leftAsMap = left.toList.lift
      val rightAsMap = right.toList.lift
      DiffResultObject(
        "List",
        indexes.map { index =>
          index.toString -> (implicitly[DiffFor[Option[T]]]
            .apply(leftAsMap(index), rightAsMap(index), toIgnore) match {
            case DiffResultValue(Some(v), None) => DiffResultAdditional(v)
            case DiffResultValue(None, Some(v)) => DiffResultMissing(v)
            case d                              => d
          })
        }.toMap
      )
    }

  implicit def diffForMap[T: DiffFor, C[_, _] <: Map[_, _]]: DiffFor[Map[String, T]] =
    (left: Map[String, T], right: Map[String, T], toIgnore: List[List[String]]) => {
      val keySet = left.keySet ++ right.keySet
      DiffResultObject("Map", keySet.map { k =>
        k -> implicitly[DiffFor[Option[T]]].apply(left.get(k), right.get(k))
      }.toMap)
    }

}
