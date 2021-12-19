package com.softwaremill.diffx

final class DiffSeqMatchByOps[C[_], T](diff: Diff[C[T]]) {
  def matchByValue[U: ObjectMatcher](f: T => U): Diff[C[T]] = {
    diff.modifyMatcherUnsafe()(ObjectMatcher.seq[T].byValue(f))
  }
}

trait DiffToSeqMatchByOps {
  implicit def toSeqMatchByOps[C[_]: SeqLike, T, S](diff: Diff[C[T]]): DiffSeqMatchByOps[C, T] =
    new DiffSeqMatchByOps[C, T](diff)
}

final class DiffSetMatchByOps[C[_], T](diff: Diff[C[T]]) {
  def matchBy[U: ObjectMatcher](f: T => U): Diff[C[T]] = {
    diff.modifyMatcherUnsafe()(ObjectMatcher.set[T].by(f))
  }
}

trait DiffToSetMatchByOps {
  implicit def toSetMatchByOps[C[_]: SetLike, T](diff: Diff[C[T]]): DiffSetMatchByOps[C, T] =
    new DiffSetMatchByOps[C, T](diff)
}

final class DiffMapMatchByOps[C[_, _], K, V](diff: Diff[C[K, V]]) {
  def matchByKey[U: ObjectMatcher](f: K => U): Diff[C[K, V]] = {
    diff.modifyMatcherUnsafe()(ObjectMatcher.map[K, V].byKey(f))
  }
  def matchByValue[U: ObjectMatcher](f: V => U): Diff[C[K, V]] = {
    diff.modifyMatcherUnsafe()(ObjectMatcher.map[K, V].byValue(f))
  }
}

trait DiffToMapMatchByOps {
  implicit def toMapMatchByOps[C[_, _]: MapLike, K, V](
      diff: Diff[C[K, V]]
  ): DiffMapMatchByOps[C, K, V] = new DiffMapMatchByOps[C, K, V](diff)
}

trait DiffToMatchByOps extends DiffToMapMatchByOps with DiffToSetMatchByOps with DiffToSeqMatchByOps
