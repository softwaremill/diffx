package com.softwaremill.diffx
import com.softwaremill.diffx.generic.{DiffMagnoliaDerivation, MagnoliaDerivedMacro}
import com.softwaremill.diffx.instances._

trait Diff[-T] { outer =>
  def apply(left: T, right: T): DiffResult = apply(left, right, DiffContext.Empty)
  def apply(left: T, right: T, context: DiffContext): DiffResult

  def contramap[R](f: R => T): Diff[R] =
    (left: R, right: R, context: DiffContext) => {
      outer(f(left), f(right), context)
    }

  def modifyUnsafe(path: String*)(diff: Diff[_]): Diff[T] =
    new Diff[T] {
      override def apply(left: T, right: T, context: DiffContext): DiffResult =
        outer.apply(left, right, context.merge(DiffContext(Tree.fromList(path.toList, diff), List.empty, Tree.empty)))
    }

  def modifyMatcherUnsafe(path: String*)(matcher: ObjectMatcher[_]): Diff[T] =
    new Diff[T] {
      override def apply(left: T, right: T, context: DiffContext): DiffResult =
        outer.apply(
          left,
          right,
          context.merge(DiffContext(Tree.empty, List.empty, Tree.fromList(path.toList, matcher)))
        )
    }
}

object Diff extends MiddlePriorityDiff with TupleInstances {
  def apply[T: Diff]: Diff[T] = implicitly[Diff[T]]

  def ignored[T]: Diff[T] = (_: T, _: T, _: DiffContext) => DiffResult.Ignored

  def compare[T: Diff](left: T, right: T): DiffResult = apply[T].apply(left, right)

  /** Create a Diff instance using [[Object#equals]] */
  def useEquals[T]: Diff[T] = Diff.fallback[T]

  def approximate[T: Numeric](epsilon: T): Diff[T] =
    new ApproximateDiffForNumeric[T](epsilon)

  def derived[T]: Derived[Diff[T]] = macro MagnoliaDerivedMacro.derivedGen[T]

  implicit val diffForString: Diff[String] = new DiffForString
  implicit val diffForRange: Diff[Range] = Diff.useEquals[Range]
  implicit val diffForChar: Diff[Char] = Diff.useEquals[Char]
  implicit val diffForBoolean: Diff[Boolean] = Diff.useEquals[Boolean]

  implicit def diffForNumeric[T: Numeric]: Diff[T] = new DiffForNumeric[T]
  implicit def diffForMap[K, V, C[KK, VV] <: scala.collection.Map[KK, VV]](implicit
      dv: Diff[V],
      dk: Diff[K],
      matcher: ObjectMatcher[(K, V)]
  ): Diff[C[K, V]] = new DiffForMap[K, V, C](matcher, dk, dv)
  implicit def diffForOptional[T](implicit ddt: Diff[T]): Diff[Option[T]] = new DiffForOption[T](ddt)
  implicit def diffForSet[T, C[W] <: scala.collection.Set[W]](implicit
      dt: Diff[T],
      matcher: ObjectMatcher[T]
  ): Diff[C[T]] = new DiffForSet[T, C](dt, matcher)
  implicit def diffForEither[L, R](implicit ld: Diff[L], rd: Diff[R]): Diff[Either[L, R]] =
    new DiffForEither[L, R](ld, rd)
}

trait MiddlePriorityDiff extends DiffMagnoliaDerivation with LowPriorityDiff {

  implicit def diffForIterable[T, C[W] <: Iterable[W]](implicit
      dt: Diff[T],
      matcher: ObjectMatcher[(Int, T)]
  ): Diff[C[T]] = new DiffForIterable[T, C](dt, matcher)
}

trait LowPriorityDiff {
  // Implicit instance of Diff[T] created from implicit Derived[Diff[T]]
  implicit def derivedDiff[T](implicit dd: Derived[Diff[T]]): Diff[T] = dd.value

  implicit class RichDerivedDiff[T](val dd: Derived[Diff[T]]) {
    def contramap[R](f: R => T): Derived[Diff[R]] = Derived(dd.value.contramap(f))

    def modify[U](path: T => U): DerivedDiffLens[T, U] = macro ModifyMacro.derivedModifyMacro[T, U]
    def ignore[U](path: T => U): Derived[Diff[T]] = macro ModifyMacro.derivedIgnoreMacro[T, U]
  }

  implicit class RichDiff[T](val d: Diff[T]) {
    def modify[U](path: T => U): DiffLens[T, U] = macro ModifyMacro.modifyMacro[T, U]
    def ignore[U](path: T => U): Diff[T] = macro ModifyMacro.ignoreMacro[T, U]
  }
}

case class Derived[T](value: T)

object Derived {
  def apply[T: Derived]: Derived[T] = implicitly[Derived[T]]
}

case class DiffLens[T, U](outer: Diff[T], path: List[String]) {
  def setTo(d: Diff[U]): Diff[T] = {
    outer.modifyUnsafe(path: _*)(d)
  }
  def ignore(): Diff[T] = outer.modifyUnsafe(path: _*)(Diff.ignored)

  def withMapMatcher[K, V](m: ObjectMatcher[(K, V)])(implicit ev1: U <:< scala.collection.Map[K, V]): Diff[T] =
    outer.modifyMatcherUnsafe(path: _*)(m)
  def withSetMatcher[V](m: ObjectMatcher[V])(implicit ev2: U <:< scala.collection.Set[V]): Diff[T] =
    outer.modifyMatcherUnsafe(path: _*)(m)
  def withListMatcher[V](m: ObjectMatcher[(Int, V)])(implicit ev3: U <:< Iterable[V]): Diff[T] =
    outer.modifyMatcherUnsafe(path: _*)(m)
}
case class DerivedDiffLens[T, U](outer: Diff[T], path: List[String]) {
  def setTo(d: Diff[U]): Derived[Diff[T]] = {
    Derived(outer.modifyUnsafe(path: _*)(d))
  }
  def ignore(): Derived[Diff[T]] = Derived(outer.modifyUnsafe(path: _*)(Diff.ignored))

  def withMapMatcher[K, V](m: ObjectMatcher[(K, V)])(implicit
      ev1: U <:< scala.collection.Map[K, V]
  ): Derived[Diff[T]] =
    Derived(outer.modifyMatcherUnsafe(path: _*)(m))
  def withSetMatcher[V](m: ObjectMatcher[V])(implicit ev2: U <:< scala.collection.Set[V]): Derived[Diff[T]] =
    Derived(outer.modifyMatcherUnsafe(path: _*)(m))
  def withListMatcher[V](m: ObjectMatcher[(Int, V)])(implicit ev3: U <:< Iterable[V]): Derived[Diff[T]] =
    Derived(outer.modifyMatcherUnsafe(path: _*)(m))
}
