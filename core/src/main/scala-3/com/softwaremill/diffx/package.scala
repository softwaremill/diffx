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

  implicit class DiffxEachMap[F[_, _], K, V](t: F[K, V])(implicit fac: Factory[(K, V), F[K, V]]) {
    @compileTimeOnly(canOnlyBeUsedInsideDiffxMacro("eachKey"))
    def eachKey: K = sys.error(canOnlyBeUsedInsideDiffxMacro("eachKey"))
    @compileTimeOnly(canOnlyBeUsedInsideDiffxMacro("eachValue"))
    def eachValue: V = sys.error(canOnlyBeUsedInsideDiffxMacro("eachValue"))
  }
}
