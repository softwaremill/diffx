package com.softwaremill.diffx.generic

import com.softwaremill.diffx.{Derived, Diff}

package object auto extends DiffDerivation

trait DiffDerivation extends DiffMagnoliaDerivation {
  implicit def diffForCaseClass[T]: Derived[Diff[T]] = macro MagnoliaDerivedMacro.derivedGen[T]

  // Implicit conversion
  implicit def unwrapDerivedDiff[T](dd: Derived[Diff[T]]): Diff[T] = dd.value
}
