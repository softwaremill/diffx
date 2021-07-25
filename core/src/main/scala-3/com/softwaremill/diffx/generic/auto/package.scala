package com.softwaremill.diffx.generic

import com.softwaremill.diffx.generic.DiffMagnoliaDerivation
import scala.deriving.Mirror
import com.softwaremill.diffx.{Diff, Derived}

package object auto extends AutoDerivation

trait AutoDerivation extends DiffMagnoliaDerivation {
  inline implicit def diffForCaseClass[T](implicit m: Mirror.Of[T]): Derived[Diff[T]] = Derived(derived[T])
}