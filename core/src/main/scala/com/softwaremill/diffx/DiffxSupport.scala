package com.softwaremill.diffx

import scala.annotation.compileTimeOnly
import com.softwaremill.diffx.DiffxSupport._

trait DiffxSupport extends DiffxEitherSupport with DiffxConsoleSupport with DiffxOptionSupport {
  type FieldPath = List[String]
  type ListMatcher[T] = ObjectMatcher[ObjectMatcher.IterableEntry[T]]
  type SetMatcher[T] = ObjectMatcher[ObjectMatcher.SetEntry[T]]
  type MapMatcher[K, V] = ObjectMatcher[ObjectMatcher.MapEntry[K, V]]

  def compare[T](left: T, right: T)(implicit d: Diff[T]): DiffResult = d.apply(left, right)

  private[diffx] def nullGuard[T](left: T, right: T)(compareNotNull: (T, T) => DiffResult): DiffResult = {
    if ((left == null && right != null) || (left != null && right == null)) {
      DiffResultValue(left, right)
    } else if (left == null && right == null) {
      IdenticalValue(null)
    } else {
      compareNotNull(left, right)
    }
  }
}

object DiffxSupport {
  private[diffx] def canOnlyBeUsedInsideIgnore(method: String) =
    s"$method can only be used inside ignore"
}

trait DiffxEitherSupport {
  implicit class DiffxEither[T[_, _], L, R](e: T[L, R])(implicit f: DiffxEitherFunctor[T, L, R]) {
//    @compileTimeOnly(canOnlyBeUsedInsideIgnore("eachLeft"))
    def eachLeft: L = sys.error("")

//    @compileTimeOnly(canOnlyBeUsedInsideIgnore("eachRight"))
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
  val noColors: ConsoleColorConfig =
    ConsoleColorConfig(default = identity, arrow = identity, right = identity, left = identity)
  val dark: ConsoleColorConfig = ConsoleColorConfig(left = magenta, right = green, default = cyan, arrow = red)
  val light: ConsoleColorConfig = ConsoleColorConfig(default = black, arrow = red, left = magenta, right = blue)
  val normal: ConsoleColorConfig =
    ConsoleColorConfig(default = identity, arrow = red, right = green, left = red)
  val envDriven: ConsoleColorConfig = Option(System.getenv("DIFFX_COLOR_THEME")) match {
    case Some("light") => light
    case Some("dark")  => dark
    case _             => normal
  }
  implicit val default: ConsoleColorConfig = handleNoColorsEnv().getOrElse(envDriven)

  private def handleNoColorsEnv(): Option[ConsoleColorConfig] = Option(System.getenv("NO_COLOR")).map(_ => noColors)

  def magenta: String => String = toColor(Console.MAGENTA)
  def green: String => String = toColor(Console.GREEN)
  def blue: String => String = toColor(Console.BLUE)
  def cyan: String => String = toColor(Console.CYAN)
  def red: String => String = toColor(Console.RED)
  def black: String => String = toColor(Console.BLACK)

  private def toColor(color: String) = { (s: String) => color + s + Console.RESET }
}

trait DiffxOptionSupport {
  implicit class DiffxEach[F[_], T](t: F[T])(implicit f: DiffxFunctor[F, T]) {
//    @compileTimeOnly(canOnlyBeUsedInsideIgnore("each"))
    def each: T = sys.error("")
  }

  trait DiffxFunctor[F[_], A] {
//    @compileTimeOnly(canOnlyBeUsedInsideIgnore("each"))
    def each(fa: F[A])(f: A => A): F[A] = sys.error("")
  }

  implicit def optionDiffxFunctor[A]: DiffxFunctor[Option, A] =
    new DiffxFunctor[Option, A] {}
}
