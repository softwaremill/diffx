package com.softwaremill.diffx

import com.softwaremill.diffx.generic.DiffMagnoliaDerivation
import magnolia.Magnolia

trait DiffCompanionMacro extends DiffMagnoliaDerivation {
  def derived[T]: Diff[T] = macro Magnolia.gen[T]

  implicit class RichDerivedDiff[T](val dd: Derived[Diff[T]]) {
    def contramap[R](f: R => T): Derived[Diff[R]] = Derived(dd.value.contramap(f))

    def modify[U](path: T => U): DerivedDiffLens[T, U] =
      macro ModifyMacro.derivedModifyMacro[T, U]
    def ignore[U](path: T => U)(implicit conf: DiffConfiguration): Derived[Diff[T]] =
      macro ModifyMacro.derivedIgnoreMacro[T, U]
  }

  /*
    `modify` and `ignore` methods have to be declared
    in invariant extension class in scala-2, as it somehow helps with the typeinference
    and makes chainging those methods possible without the need to specify generic parameters
   */
  implicit class RichDiff[T](val d: Diff[T]) {
    def modify[U](path: T => U): DiffLens[T, U] = macro ModifyMacro.modifyMacro[T, U]
    def ignore[U](path: T => U)(implicit conf: DiffConfiguration): Diff[T] = macro ModifyMacro.ignoreMacro[T, U]
  }
}
