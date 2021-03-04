package com.softwaremill.diffx
import com.softwaremill.diffx.generic.{DiffMagnoliaDerivation, MagnoliaDerivedMacro}
import com.softwaremill.diffx.instances._
import magnolia.Magnolia

trait Diff[-T] { outer =>
  def apply(left: T, right: T): DiffResult = apply(left, right, Nil)
  def apply(left: T, right: T, toIgnore: List[FieldPath]): DiffResult

  def contramap[R](f: R => T): Diff[R] =
    (left: R, right: R, toIgnore: List[FieldPath]) => {
      outer(f(left), f(right), toIgnore)
    }

  def ignore[S <: T, U](path: S => U): Diff[S] = macro IgnoreMacro.ignoreMacro[S, U]

  def ignoreUnsafe(fields: String*): Diff[T] =
    new Diff[T] {
      override def apply(left: T, right: T, toIgnore: List[FieldPath]): DiffResult =
        outer.apply(left, right, toIgnore ++ List(fields.toList))
    }
}

object Diff extends MiddlePriorityDiff {
  def apply[T: Diff]: Diff[T] = implicitly[Diff[T]]

  def identical[T]: Diff[T] = (left: T, _: T, _: List[FieldPath]) => Identical(left)

  def compare[T: Diff](left: T, right: T): DiffResult = apply[T].apply(left, right)

  /** Create a Diff instance using [[Object#equals]] */
  def useEquals[T]: Diff[T] = Diff.fallback[T]

  def derived[T]: Derived[Diff[T]] = macro MagnoliaDerivedMacro.derivedGen[T]

  implicit val diffForString: Diff[String] = new DiffForString
  implicit val diffForRange: Diff[Range] = Diff.fallback[Range]
  implicit def diffForNumeric[T: Numeric]: Diff[T] = new DiffForNumeric[T]
  implicit def diffForMap[K, V, C[KK, VV] <: scala.collection.Map[KK, VV]](implicit
      ddot: Diff[Option[V]],
      ddk: Diff[K],
      matcher: ObjectMatcher[K]
  ): Diff[C[K, V]] = new DiffForMap[K, V, C](matcher, ddk, ddot)
  implicit def diffForOptional[T](implicit ddt: Diff[T]): Diff[Option[T]] = new DiffForOption[T](ddt)
  implicit def diffForSet[T, C[W] <: scala.collection.Set[W]](implicit
      ddt: Diff[T],
      matcher: ObjectMatcher[T]
  ): Diff[C[T]] = new DiffForSet[T, C](ddt, matcher)
}

trait MiddlePriorityDiff extends DiffMagnoliaDerivation with LowPriorityDiff {

  implicit def diffForIterable[T, C[W] <: Iterable[W]](implicit
      ddot: Diff[Option[T]]
  ): Diff[C[T]] = new DiffForIterable[T, C](ddot)
}

trait LowPriorityDiff {
  // Implicit instance of Diff[T] created from implicit Derived[Diff[T]]
  implicit def derivedDiff[T](implicit dd: Derived[Diff[T]]): Diff[T] = dd.value

  implicit class RichDerivedDiff[T](val dd: Derived[Diff[T]]) {
    def contramap[R](f: R => T): Derived[Diff[R]] = Derived(dd.value.contramap(f))

    def ignore[S <: T, U](path: S => U): Derived[Diff[S]] = macro IgnoreMacro.derivedIgnoreMacro[S, U]
  }
}

case class Derived[T](value: T)

object Derived {
  def apply[T: Derived]: Derived[T] = implicitly[Derived[T]]
}
