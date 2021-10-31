package com.softwaremill.diffx.generic

import com.softwaremill.diffx.generic.DiffMagnoliaDerivation
import com.softwaremill.diffx.generic.auto.DiffAutoDerivationOn

import scala.deriving.Mirror
import com.softwaremill.diffx.{Derived, Diff}

package object auto extends AutoDerivation

trait AutoDerivation extends DiffMagnoliaDerivation {
  inline given diffForCaseClass[T](using Mirror.Of[T]): Derived[Diff[T]] = Derived(derived[T])

  given indicator: DiffAutoDerivationOn = new DiffAutoDerivationOn {}
}