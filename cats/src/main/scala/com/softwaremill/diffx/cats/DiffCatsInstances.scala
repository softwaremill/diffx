package com.softwaremill.diffx.cats

import cats.data.{Chain, NonEmptyChain, NonEmptyList, NonEmptyMap, NonEmptySet, NonEmptyVector}
import com.softwaremill.diffx.instances.{DiffForMap, DiffForSeq, DiffForSet}
import com.softwaremill.diffx.{Diff, MapLike, MapMatcher, SeqLike, SeqMatcher, SetLike, SetMatcher}

trait DiffCatsInstances {
  implicit def diffNel[T: Diff](implicit
      seqMatcher: SeqMatcher[T],
      seqLike: SeqLike[NonEmptyList]
  ): Diff[NonEmptyList[T]] =
    new DiffForSeq[NonEmptyList, T](Diff[T], seqMatcher, seqLike, "NonEmptyList")

  implicit def nelIsLikeSeq: SeqLike[NonEmptyList] = new SeqLike[NonEmptyList] {
    override def asSeq[A](c: NonEmptyList[A]): Seq[A] = c.toList
  }

  implicit def diffChain[T: Diff](implicit
      seqMatcher: SeqMatcher[T],
      seqLike: SeqLike[Chain]
  ): Diff[Chain[T]] =
    new DiffForSeq[Chain, T](Diff[T], seqMatcher, seqLike, "Chain")

  implicit def diffNec[T: Diff](implicit
      seqMatcher: SeqMatcher[T],
      seqLike: SeqLike[NonEmptyChain]
  ): Diff[NonEmptyChain[T]] =
    new DiffForSeq[NonEmptyChain, T](Diff[T], seqMatcher, seqLike, "NonEmptyChain")

  implicit def chainIsLikeSeq: SeqLike[Chain] = new SeqLike[Chain] {
    override def asSeq[A](c: Chain[A]): Seq[A] = c.toList
  }

  implicit def necIsLikeSeq: SeqLike[NonEmptyChain] = new SeqLike[NonEmptyChain] {
    override def asSeq[A](c: NonEmptyChain[A]): Seq[A] = c.toChain.toList
  }

  implicit def diffNev[T: Diff](implicit
      seqMatcher: SeqMatcher[T],
      seqLike: SeqLike[NonEmptyVector]
  ): Diff[NonEmptyVector[T]] =
    new DiffForSeq[NonEmptyVector, T](Diff[T], seqMatcher, seqLike, "NonEmptyVector")

  implicit def nevIsLikeSeq: SeqLike[NonEmptyVector] = new SeqLike[NonEmptyVector] {
    override def asSeq[A](c: NonEmptyVector[A]): Seq[A] = c.toVector
  }

  implicit def diffNes[T: Diff](implicit
      setMatcher: SetMatcher[T],
      setLike: SetLike[NonEmptySet]
  ): Diff[NonEmptySet[T]] =
    new DiffForSet[NonEmptySet, T](Diff[T], setMatcher, setLike, "NonEmptySet")

  implicit def nesIsLikeSet: SetLike[NonEmptySet] = new SetLike[NonEmptySet] {
    override def asSet[A](c: NonEmptySet[A]): Set[A] = c.toSortedSet
  }

  implicit def diffNem[K: Diff, V: Diff](implicit
      mapMatcher: MapMatcher[K, V],
      mapLike: MapLike[NonEmptyMap]
  ): Diff[NonEmptyMap[K, V]] = new DiffForMap[NonEmptyMap, K, V](mapMatcher, Diff[K], Diff[V], mapLike, "NonEmptyMap")

  implicit def nemIsLikeMap: MapLike[NonEmptyMap] = new MapLike[NonEmptyMap] {
    override def asMap[K, V](c: NonEmptyMap[K, V]): Map[K, V] = c.toSortedMap
  }
}
