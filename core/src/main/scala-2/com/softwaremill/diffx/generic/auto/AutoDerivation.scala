package com.softwaremill.diffx.generic

import com.softwaremill.diffx.generic.auto.MagnoliaDerivedMacro
import com.softwaremill.diffx.{Derived, Diff}

package object auto extends AutoDerivation

trait AutoDerivation extends DiffMagnoliaDerivation {
  implicit def diffForCaseClass[T]: Derived[Diff[T]] = macro MagnoliaDerivedMacro.derivedGen[T]

  // Implicit conversion
  implicit def unwrapDerivedDiff[T](dd: Derived[Diff[T]]): Diff[T] = dd.value
}
