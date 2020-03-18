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
  def leftColor(s: String)(implicit c: ConsoleColorConfig): String = c.left(s)
  def rightColor(s: String)(implicit c: ConsoleColorConfig): String = c.right(s)
  def defaultColor(s: String)(implicit c: ConsoleColorConfig): String = c.default(s)
  def arrowColor(s: String)(implicit c: ConsoleColorConfig): String = c.arrow(s)
  def showChange(l: String, r: String)(implicit c: ConsoleColorConfig): String =
    leftColor(l) + arrowColor(" -> ") + rightColor(r)
}

case class ConsoleColorConfig(
    left: String => String,
    right: String => String,
    default: String => String,
    arrow: String => String
)

object ConsoleColorConfig {
  val dark: ConsoleColorConfig =
    ConsoleColorConfig(left = magenta, right = green, default = cyan, arrow = red)

  val light: ConsoleColorConfig = ConsoleColorConfig(default = black, arrow = red, left = magenta, right = blue)

  val envDriven: ConsoleColorConfig = ConsoleColorConfig(
    default = Option(System.getenv("DIFFX_DEFAULT_COLOR")).map(toColor).getOrElse(dark.default),
    left = Option(System.getenv("DIFFX_LEFT_COLOR")).map(toColor).getOrElse(dark.left),
    right = Option(System.getenv("DIFFX_RIGHT_COLOR")).map(toColor).getOrElse(dark.right),
    arrow = Option(System.getenv("DIFFX_ARROW_COLOR")).map(toColor).getOrElse(dark.arrow)
  )
  implicit val default: ConsoleColorConfig = envDriven

  def magenta(s: String): String = Console.MAGENTA + s + Console.RESET
  def green(s: String): String = Console.GREEN + s + Console.RESET
  def blue(s: String): String = Console.BLUE + s + Console.RESET
  def cyan(s: String): String = Console.CYAN + s + Console.RESET
  def red(s: String): String = Console.RED + s + Console.RESET
  def black(s: String): String = Console.BLACK + s + Console.RESET

  private def toColor(color: String) = { s: String => color + s + Console.RESET }
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
