package com.softwaremill.diffx

trait DiffxSupport extends DiffxEitherSupport with DiffxOptionSupport with DiffLensToMatchByOps with DiffToMatchByOps {
  type FieldPath = List[String]
  type SeqMatcher[T] = ObjectMatcher[ObjectMatcher.SeqEntry[T]]
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

  trait DiffxSubtypeSelector[T] {
    def subtype[S <: T]: S = sys.error("")
  }

  implicit def toSubtypeSelector[A](a: A): DiffxSubtypeSelector[A] = new DiffxSubtypeSelector[A] {}
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

case class ShowConfig(
    left: String => String,
    right: String => String,
    default: String => String,
    arrow: String => String,
    transformer: DiffResultTransformer
) {
  def skipIdentical: ShowConfig = this.copy(transformer = DiffResultTransformer.skipIdentical)
}

object ShowConfig {
  val noColors: ShowConfig =
    ShowConfig(
      default = identity,
      arrow = identity,
      right = identity,
      left = identity,
      transformer = identity(_)
    )
  val dark: ShowConfig = ShowConfig(
    left = magenta,
    right = green,
    default = cyan,
    arrow = red,
    transformer = identity(_)
  )
  val light: ShowConfig = ShowConfig(
    default = black,
    arrow = red,
    left = magenta,
    right = blue,
    transformer = identity(_)
  )
  val normal: ShowConfig =
    ShowConfig(
      default = identity,
      arrow = red,
      right = green,
      left = red,
      transformer = identity(_)
    )
  val envDriven: ShowConfig = Option(System.getenv("DIFFX_COLOR_THEME")) match {
    case Some("light") => light
    case Some("dark")  => dark
    case _             => normal
  }
  implicit val default: ShowConfig = handleNoColorsEnv().getOrElse(envDriven)

  private def handleNoColorsEnv(): Option[ShowConfig] = Option(System.getenv("NO_COLOR")).map(_ => noColors)

  def magenta: String => String = toColor(Console.MAGENTA)
  def green: String => String = toColor(Console.GREEN)
  def blue: String => String = toColor(Console.BLUE)
  def cyan: String => String = toColor(Console.CYAN)
  def red: String => String = toColor(Console.RED)
  def black: String => String = toColor(Console.BLACK)

  private def toColor(color: String) = { (s: String) => color + s + Console.RESET }
}

trait DiffResultTransformer {
  def apply(diffResult: DiffResult): DiffResult
}

object DiffResultTransformer {
  val skipIdentical: DiffResultTransformer = {
    case d: DiffResultObject => d.copy(fields = d.fields.filter { case (_, v) => !v.isIdentical })
    case d: DiffResultMap =>
      d.copy(entries = d.entries.filter { case (k, v) => !v.isIdentical || !k.isIdentical })
    case d: DiffResultSet      => d.copy(diffs = d.diffs.filter(df => !df.isIdentical))
    case d: DiffResultIterable => d.copy(items = d.items.filter { case (_, v) => !v.isIdentical })
    case other                 => other
  }
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
