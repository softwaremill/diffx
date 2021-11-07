package com.softwaremill

import scala.annotation.compileTimeOnly
import scala.collection.Factory
import com.softwaremill.diffx.DiffxSupport._

package object diffx extends DiffxSupport {
  implicit def traversableDiffxFunctor[F[_], A](implicit
      fac: Factory[A, F[A]],
      ev: F[A] => Iterable[A]
  ): DiffxFunctor[F, A] =
    new DiffxFunctor[F, A] {}

  implicit class DiffxEachMap[F[_, _], K, T](t: F[K, T])(implicit fac: Factory[(K, T), F[K, T]]) {
    @compileTimeOnly(canOnlyBeUsedInsideIgnore("eachKey"))
    def eachKey: K = sys.error("")
    @compileTimeOnly(canOnlyBeUsedInsideIgnore("eachValue"))
    def eachValue: T = sys.error("")
  }
}
