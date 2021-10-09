package com.softwaremill.diffx.generic

import com.softwaremill.diffx.generic.DiffMagnoliaDerivation
import scala.deriving.Mirror
import com.softwaremill.diffx.{Diff, Derived}

package object auto extends AutoDerivation

trait AutoDerivation extends DiffMagnoliaDerivation {
  inline given diffForCaseClass[T](using Mirror.Of[T]): Derived[Diff[T]] = Derived(derived[T])

//  // Implicit conversion
//  implicit def unwrapDerivedDiff[T](dd: Derived[Diff[T]]): Diff[T] = dd.value

  inline given unwrapDerivedDiff[T] : Conversion[Derived[Diff[T]], Diff[T]] = _.value
}