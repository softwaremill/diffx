package com.softwaremill

import scala.annotation.compileTimeOnly
import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom
import scala.language.experimental.macros
import scala.language.higherKinds

package object diffx {
  def red(s: String): String = Console.RED + s + Console.RESET

  def green(s: String): String = Console.GREEN + s + Console.RESET

  def blue(s: String): String = Console.BLUE + s + Console.RESET

  def pad(s: Any, i: Int = 5): String = (" " * (i - s.toString.length)) + s

  def arrow(l: String, r: String): String = l + " -> " + r

  def showChange(l: String, r: String): String = red(l) + " -> " + green(r)

  implicit class QuicklensEach[F[_], T](t: F[T])(implicit f: QuicklensFunctor[F, T]) {
    @compileTimeOnly(canOnlyBeUsedInsideModify("each"))
    def each: T = sys.error("")
  }

  private def canOnlyBeUsedInsideModify(method: String) =
    s"$method can only be used inside modify"

  trait QuicklensFunctor[F[_], A] {
    def map(fa: F[A])(f: A => A): F[A]

    def each(fa: F[A])(f: A => A): F[A] = map(fa)(f)
  }

  implicit def optionQuicklensFunctor[A]: QuicklensFunctor[Option, A] =
    new QuicklensFunctor[Option, A] {
      override def map(fa: Option[A])(f: A => A) = fa.map(f)
    }

  implicit def traversableQuicklensFunctor[F[_], A](
      implicit cbf: CanBuildFrom[F[A], A, F[A]],
      ev: F[A] => TraversableLike[A, F[A]]
  ): QuicklensFunctor[F, A] =
    new QuicklensFunctor[F, A] {
      override def map(fa: F[A])(f: A => A) = fa.map(f)
    }

  implicit class QuicklensEither[T[_, _], L, R](e: T[L, R])(implicit f: QuicklensEitherFunctor[T, L, R]) {
    @compileTimeOnly(canOnlyBeUsedInsideModify("eachLeft"))
    def eachLeft: L = sys.error("")

    @compileTimeOnly(canOnlyBeUsedInsideModify("eachRight"))
    def eachRight: R = sys.error("")
  }

  trait QuicklensEitherFunctor[T[_, _], L, R] {
    def eachLeft(e: T[L, R])(f: L => L): T[L, R]

    def eachRight(e: T[L, R])(f: R => R): T[L, R]
  }

  implicit def eitherQuicklensFunctor[T[_, _], L, R]: QuicklensEitherFunctor[Either, L, R] =
    new QuicklensEitherFunctor[Either, L, R] {
      override def eachLeft(e: Either[L, R])(f: (L) => L) = e.left.map(f)

      override def eachRight(e: Either[L, R])(f: (R) => R) = e.right.map(f)
    }

  trait QuicklensMapAtFunctor[F[_, _], K, T] {
    def each(fa: F[K, T])(f: T => T): F[K, T]
  }

  implicit def mapQuicklensFunctor[M[KT, TT] <: Map[KT, TT], K, T](
      implicit cbf: CanBuildFrom[M[K, T], (K, T), M[K, T]]
  ): QuicklensMapAtFunctor[M, K, T] = new QuicklensMapAtFunctor[M, K, T] {
    override def each(fa: M[K, T])(f: (T) => T) = {
      val builder = cbf(fa)
      fa.foreach { case (k, t) => builder += k -> f(t) }
      builder.result
    }
  }

  implicit class QuicklensEachMap[F[_, _], K, T](t: F[K, T])(implicit f: QuicklensMapAtFunctor[F, K, T]) {
    @compileTimeOnly(canOnlyBeUsedInsideModify("each"))
    def each: T = sys.error("")
  }
}
