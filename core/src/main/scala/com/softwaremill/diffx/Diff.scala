package com.softwaremill.diffx

import com.softwaremill.diffx.ObjectMatcher.{IterableEntry, MapEntry, SetEntry}
import com.softwaremill.diffx.instances._

trait Diff[T] { outer =>
  def apply(left: T, right: T): DiffResult = apply(left, right, DiffContext.Empty)
  def apply(left: T, right: T, context: DiffContext): DiffResult

  def contramap[R](f: R => T): Diff[R] =
    (left: R, right: R, context: DiffContext) => {
      outer(f(left), f(right), context)
    }

  def modifyUnsafe[U](path: String*)(mod: Diff[U] => Diff[U]): Diff[T] =
    new Diff[T] {
      override def apply(left: T, right: T, context: DiffContext): DiffResult =
        outer.apply(
          left,
          right,
          context.merge(
            DiffContext.atPath(path.toList, mod.asInstanceOf[Diff[Any] => Diff[Any]])
          )
        )
    }

  def modifyMatcherUnsafe(path: String*)(matcher: ObjectMatcher[_]): Diff[T] =
    new Diff[T] {
      override def apply(left: T, right: T, context: DiffContext): DiffResult =
        outer.apply(
          left,
          right,
          context.merge(DiffContext.atPath(path.toList, matcher))
        )
    }
}

object Diff extends LowPriorityDiff with DiffTupleInstances with DiffxPlatformExtensions with DiffCompanionMacro {
  def apply[T: Diff]: Diff[T] = implicitly[Diff[T]]

  def ignored[T]: Diff[T] = (_: T, _: T, _: DiffContext) => DiffResult.Ignored

  def compare[T: Diff](left: T, right: T): DiffResult = apply[T].apply(left, right)

  /** Create a Diff instance using [[Object#equals]] */
  def useEquals[T]: Diff[T] = (left: T, right: T, _: DiffContext) => {
      if (left != right) {
        DiffResultValue(left, right)
      } else {
        IdenticalValue(left)
      }
    }

  def approximate[T: Numeric](epsilon: T): Diff[T] =
    new ApproximateDiffForNumeric[T](epsilon)

  implicit val diffForString: Diff[String] = new DiffForString
  implicit val diffForRange: Diff[Range] = Diff.useEquals[Range]
  implicit val diffForChar: Diff[Char] = Diff.useEquals[Char]
  implicit val diffForBoolean: Diff[Boolean] = Diff.useEquals[Boolean]

  implicit def diffForNumeric[T: Numeric]: Diff[T] = new DiffForNumeric[T]

  implicit def diffForMap[K, V, C[KK, VV] <: scala.collection.Map[KK, VV]](implicit
      dv: Diff[V],
      dk: Diff[K],
      matcher: ObjectMatcher[MapEntry[K, V]]
  ): Diff[C[K, V]] = new DiffForMap[K, V, C](matcher, dk, dv)

  implicit def diffForOptional[T](implicit ddt: Diff[T]): Diff[Option[T]] = new DiffForOption[T](ddt)

  implicit def diffForSet[T, C[W] <: scala.collection.Set[W]](implicit
      dt: Diff[T],
      matcher: ObjectMatcher[SetEntry[T]]
  ): Diff[C[T]] = new DiffForSet[T, C](dt, matcher)

  implicit def diffForEither[L, R](implicit ld: Diff[L], rd: Diff[R]): Diff[Either[L, R]] =
    new DiffForEither[L, R](ld, rd)
}

trait LowPriorityDiff {

  implicit def diffForIterable[T, C[W] <: Iterable[W]](implicit
      dt: Diff[T],
      matcher: ObjectMatcher[IterableEntry[T]]
  ): Diff[C[T]] = new DiffForIterable[T, C](dt, matcher)

  // Implicit instance of Diff[T] created from implicit Derived[Diff[T]]
  implicit def derivedDiff[T](implicit dd: Derived[Diff[T]]): Diff[T] = dd.value
}

case class Derived[T](value: T)

object Derived {
  def apply[T: Derived]: Derived[T] = implicitly[Derived[T]]
}

case class DiffLens[T, U](outer: Diff[T], path: List[String]) extends DiffLensMacro[T, U] {
  def setTo(d: Diff[U]): Diff[T] = using(_ => d)

  def using(mod: Diff[U] => Diff[U]): Diff[T] = {
    outer.modifyUnsafe(path: _*)(mod)
  }

  def ignore(implicit config: DiffConfiguration): Diff[T] = outer.modifyUnsafe(path: _*)(config.makeIgnored)
}

case class DerivedDiffLens[T, U](outer: Diff[T], path: List[String]) extends DerivedDiffLensMacro[T, U] {
  def setTo(d: Diff[U]): Derived[Diff[T]] = using(_ => d)

  def using(mod: Diff[U] => Diff[U]): Derived[Diff[T]] = {
    Derived(outer.modifyUnsafe(path: _*)(mod))
  }

  def ignore(implicit config: DiffConfiguration): Derived[Diff[T]] = Derived(
    outer.modifyUnsafe(path: _*)(config.makeIgnored)
  )
}
