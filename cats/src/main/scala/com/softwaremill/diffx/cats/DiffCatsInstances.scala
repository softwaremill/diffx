package com.softwaremill.diffx.cats

import cats.data.{NonEmptyChain, NonEmptyList, NonEmptySet, NonEmptyVector}
import com.softwaremill.diffx.Diff
import com.softwaremill.diffx.Diff._

trait DiffCatsInstances {
  implicit def diffNel[T: Diff]: Diff[NonEmptyList[T]] =
    Diff[List[T]].contramap[NonEmptyList[T]](_.toList)

  implicit def diffNec[T: Diff]: Diff[NonEmptyChain[T]] =
    Diff[List[T]].contramap[NonEmptyChain[T]](_.toChain.toList)

  implicit def diffNes[T: Diff]: Diff[NonEmptySet[T]] =
    Diff[Set[T]].contramap[NonEmptySet[T]](_.toSortedSet)

  implicit def diffNev[T: Diff]: Diff[NonEmptyVector[T]] =
    Diff[List[T]].contramap[NonEmptyVector[T]](_.toVector.toList)
}
