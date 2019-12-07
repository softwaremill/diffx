package com.softwaremill.diffx
import acyclic.skipped

trait Diff[T] { outer =>
  def apply(left: T, right: T): DiffResult = apply(left, right, Nil)
  def apply(left: T, right: T, toIgnore: List[FieldPath]): DiffResult

  def contramap[R](f: R => T): Diff[R] = (left: R, right: R, toIgnore: List[FieldPath]) => {
    outer(f(left), f(right), toIgnore)
  }

  def ignore[U](path: T => U): Diff[T] = macro IgnoreMacro.ignoreMacro[T, U]

  def ignoreUnsafe(fields: String*): Diff[T] = new Diff[T] {
    override def apply(left: T, right: T, toIgnore: List[FieldPath]): DiffResult =
      outer.apply(left, right, toIgnore ++ List(fields.toList))
  }
}

object Diff extends DiffInstances {
  def apply[T: Diff]: Diff[T] = implicitly[Diff[T]]

  def identical[T]: Diff[T] = (left: T, _: T, _: List[FieldPath]) => Identical(left)
  
  def compare[T: Diff](left: T, right: T): DiffResult = apply[T].apply(left, right)

  // Implicit instance of Diff[T] created from implicit Derived[Diff[T]]
  implicit def anyDiff[T](implicit dd: Derived[Diff[T]]): Diff[T] = dd.value

  // Implicit conversion
  implicit def unwrapDerivedDiff[T](dd: Derived[Diff[T]]): Diff[T] = dd.value
}

case class Derived[T](value: T)

object Derived {
  def apply[T: Derived]: Derived[T] = implicitly[Derived[T]]
}
