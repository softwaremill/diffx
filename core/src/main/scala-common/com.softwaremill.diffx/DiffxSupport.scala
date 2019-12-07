package com.softwaremill.diffx
import acyclic.skipped

import scala.annotation.compileTimeOnly
import com.softwaremill.diffx.DiffxSupport._

trait DiffxSupport extends DiffxEitherSupport with DiffxConsoleSupport with DiffxOptionSupport {
  type FieldPath = List[String]

  def compare[T](left: T, right: T)(implicit d: Diff[T]): DiffResult = d.apply(left, right)
}

object DiffxSupport {
  private[diffx] def canOnlyBeUsedInsideIgnore(method: String) =
    s"$method can only be used inside ignore"
}

trait DiffxEitherSupport {
  implicit class DiffxEither[T[_, _], L, R](e: T[L, R])(implicit f: DiffxEitherFunctor[T, L, R]) {
    @compileTimeOnly(canOnlyBeUsedInsideIgnore("eachLeft"))
    def eachLeft: L = sys.error("")

    @compileTimeOnly(canOnlyBeUsedInsideIgnore("eachRight"))
    def eachRight: R = sys.error("")
  }

  trait DiffxEitherFunctor[T[_, _], L, R] {
    def eachLeft(e: T[L, R])(f: L => L): T[L, R] = sys.error("")
    def eachRight(e: T[L, R])(f: R => R): T[L, R] = sys.error("")
  }

  implicit def eitherDiffxFunctor[T[_, _], L, R]: DiffxEitherFunctor[Either, L, R] =
    new DiffxEitherFunctor[Either, L, R] {}
}

trait DiffxConsoleSupport {
  def red(s: String): String = Console.RED + s + Console.RESET
  def green(s: String): String = Console.GREEN + s + Console.RESET
  def blue(s: String): String = Console.BLUE + s + Console.RESET
  def pad(s: Any, i: Int = 5): String = (" " * (i - s.toString.length)) + s
  def arrow(l: String, r: String): String = l + " -> " + r
  def showChange(l: String, r: String): String = red(l) + " -> " + green(r)
}

trait DiffxOptionSupport {
  implicit class DiffxEach[F[_], T](t: F[T])(implicit f: DiffxFunctor[F, T]) {
    @compileTimeOnly(canOnlyBeUsedInsideIgnore("each"))
    def each: T = sys.error("")
  }

  trait DiffxFunctor[F[_], A] {
    @compileTimeOnly(canOnlyBeUsedInsideIgnore("each"))
    def each(fa: F[A])(f: A => A): F[A] = sys.error("")
  }

  implicit def optionDiffxFunctor[A]: DiffxFunctor[Option, A] =
    new DiffxFunctor[Option, A] {}
}
