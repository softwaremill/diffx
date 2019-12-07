package com.softwaremill
import acyclic.skipped

import scala.annotation.compileTimeOnly
import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom
import scala.language.experimental.macros
import scala.language.higherKinds
import com.softwaremill.diffx.DiffxSupport._

package object diffx extends DiffxSupport {
  implicit def traversableDiffxFunctor[F[_], A](
      implicit cbf: CanBuildFrom[F[A], A, F[A]],
      ev: F[A] => TraversableLike[A, F[A]]
  ): DiffxFunctor[F, A] =
    new DiffxFunctor[F, A] {}

  trait DiffxMapAtFunctor[F[_, _], K, T] {
    @compileTimeOnly(canOnlyBeUsedInsideIgnore("each"))
    def each(fa: F[K, T])(f: T => T): F[K, T] = sys.error("")
  }

  implicit def mapDiffxFunctor[M[KT, TT] <: Map[KT, TT], K, T](
      implicit cbf: CanBuildFrom[M[K, T], (K, T), M[K, T]]
  ): DiffxMapAtFunctor[M, K, T] = new DiffxMapAtFunctor[M, K, T] {}

  implicit class DiffxEachMap[F[_, _], K, T](t: F[K, T])(implicit f: DiffxMapAtFunctor[F, K, T]) {
    @compileTimeOnly(canOnlyBeUsedInsideIgnore("each"))
    def each: T = sys.error("")
  }
}
