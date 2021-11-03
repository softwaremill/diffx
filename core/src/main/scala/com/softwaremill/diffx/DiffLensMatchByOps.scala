package com.softwaremill.diffx

final class DiffLensSeqMatchByOps[C[_], T, S](lens: DiffLens[S, C[T]]) {
  def matchByValue[U: ObjectMatcher](f: T => U): Diff[S] = {
    lens.outer.modifyMatcherUnsafe(lens.path: _*)(ObjectMatcher.seq[T].byValue(f))
  }
}

trait DiffLensToSeqMatchByOps {
  implicit def lensToSeqMatchByOps[C[_]: SeqLike, T, S](diffLens: DiffLens[S, C[T]]): DiffLensSeqMatchByOps[C, T, S] =
    new DiffLensSeqMatchByOps[C, T, S](diffLens)
}

final class DiffLensSetMatchByOps[C[_], T, S](lens: DiffLens[S, C[T]]) {
  def matchBy[U: ObjectMatcher](f: T => U): Diff[S] = {
    lens.outer.modifyMatcherUnsafe(lens.path: _*)(ObjectMatcher.set[T].by(f))
  }
}

trait DiffLensToSetMatchByOps {
  implicit def lensToSetMatchByOps[C[_]: SetLike, T, S](diffLens: DiffLens[S, C[T]]): DiffLensSetMatchByOps[C, T, S] =
    new DiffLensSetMatchByOps[C, T, S](diffLens)
}

final class DiffLensMapMatchByOps[C[_, _], K, V, S](lens: DiffLens[S, C[K, V]]) {
  def matchByKey[U: ObjectMatcher](f: K => U): Diff[S] = {
    lens.outer.modifyMatcherUnsafe(lens.path: _*)(ObjectMatcher.map[K, V].byKey(f))
  }
  def matchByValue[U: ObjectMatcher](f: V => U): Diff[S] = {
    lens.outer.modifyMatcherUnsafe(lens.path: _*)(ObjectMatcher.map[K, V].byValue(f))
  }
}

trait DiffLensToMapMatchByOps {
  implicit def lensToMapMatchByOps[C[_, _]: MapLike, K, V, S](
      diffLens: DiffLens[S, C[K, V]]
  ): DiffLensMapMatchByOps[C, K, V, S] =
    new DiffLensMapMatchByOps[C, K, V, S](diffLens)
}

trait DiffLensToMatchByOps extends DiffLensToMapMatchByOps with DiffLensToSetMatchByOps with DiffLensToSeqMatchByOps
