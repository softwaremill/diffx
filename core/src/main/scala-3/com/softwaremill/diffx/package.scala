package com.softwaremill

import com.softwaremill.diffx.DiffxSupport.*

import scala.annotation.compileTimeOnly
import scala.collection.Factory

package object diffx extends DiffxSupport {
  implicit def traversableDiffxFunctor[F[_], A](implicit
      fac: Factory[A, F[A]],
      ev: F[A] => Iterable[A]
  ): DiffxFunctor[F, A] =
    new DiffxFunctor[F, A] {}

  implicit class DiffxEachMap[F[_, _], K, T](t: F[K, T])(implicit fac: Factory[(K, T), F[K, T]]) {
    // @compileTimeOnly(canOnlyBeUsedInsideIgnore("each"))
    def each: T = sys.error("")
  }
}
