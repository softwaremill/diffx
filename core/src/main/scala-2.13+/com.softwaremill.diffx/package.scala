package com.softwaremill

import scala.annotation.compileTimeOnly
import scala.collection.Factory

package object diffx {
  def red(s: String): String = Console.RED + s + Console.RESET
  def green(s: String): String = Console.GREEN + s + Console.RESET
  def blue(s: String): String = Console.BLUE + s + Console.RESET
  def pad(s: Any, i: Int = 5): String = (" " * (i - s.toString.length)) + s
  def arrow(l: String, r: String): String = l + " -> " + r
  def showChange(l: String, r: String): String = red(l) + " -> " + green(r)

  trait DiffxFunctor[F[_], A] {
    @compileTimeOnly(canOnlyBeUsedInsideModify("each"))
    def each(fa: F[A])(f: A => A): F[A] = sys.error("")
  }

  implicit class DiffxEach[F[_], T](t: F[T])(implicit f: DiffxFunctor[F, T]) {
    @compileTimeOnly(canOnlyBeUsedInsideModify("each"))
    def each: T = sys.error("")
  }

  private def canOnlyBeUsedInsideModify(method: String) =
    s"$method can only be used inside ignore"

  implicit def optionDiffxFunctor[A]: DiffxFunctor[Option, A] = new DiffxFunctor[Option, A] {}

  implicit def traversableDiffxFunctor[F[_], A](
      implicit fac: Factory[A, F[A]],
      ev: F[A] => Iterable[A]
  ): DiffxFunctor[F, A] =
    new DiffxFunctor[F, A] {}

  implicit class DiffxEither[T[_, _], L, R](e: T[L, R])(implicit f: DiffxEitherFunctor[T, L, R]) {
    @compileTimeOnly(canOnlyBeUsedInsideModify("eachLeft"))
    def eachLeft: L = sys.error("")

    @compileTimeOnly(canOnlyBeUsedInsideModify("eachRight"))
    def eachRight: R = sys.error("")
  }

  trait DiffxEitherFunctor[T[_, _], L, R] {
    def eachLeft(e: T[L, R])(f: L => L): T[L, R] = sys.error("")
    def eachRight(e: T[L, R])(f: R => R): T[L, R] = sys.error("")
  }

  implicit def eitherDiffxFunctor[T[_, _], L, R]: DiffxEitherFunctor[Either, L, R] =
    new DiffxEitherFunctor[Either, L, R] {}

  implicit class DiffxEachMap[F[_, _], K, T](t: F[K, T])(implicit fac: Factory[(K, T), F[K, T]]) {
    @compileTimeOnly(canOnlyBeUsedInsideModify("each"))
    def each: T = sys.error("")
  }

}
