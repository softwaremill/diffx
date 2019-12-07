package com.softwaremill.diffx.cats

import cats.data.{NonEmptyChain, NonEmptyList, NonEmptySet, NonEmptyVector}
import com.softwaremill.diffx.{Derived, Diff}
import com.softwaremill.diffx.Diff._

trait DiffCatsInstances {
  implicit def diffNel[T: Diff]: Derived[Diff[NonEmptyList[T]]] =
    Derived(Diff[List[T]].contramap[NonEmptyList[T]](_.toList))

  implicit def diffNec[T: Diff]: Derived[Diff[NonEmptyChain[T]]] =
    Derived(Diff[List[T]].contramap[NonEmptyChain[T]](_.toChain.toList))

  implicit def diffNes[T: Diff]: Derived[Diff[NonEmptySet[T]]] =
    Derived(Diff[Set[T]].contramap[NonEmptySet[T]](_.toSortedSet))

  implicit def diffNev[T: Diff]: Derived[Diff[NonEmptyVector[T]]] =
    Derived(Diff[List[T]].contramap[NonEmptyVector[T]](_.toVector.toList))
}
