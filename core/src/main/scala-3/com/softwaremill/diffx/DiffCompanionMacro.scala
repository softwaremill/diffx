package com.softwaremill.diffx

import com.softwaremill.diffx.generic.DiffMagnoliaDerivation
import com.softwaremill.diffx.generic.auto.DiffAutoDerivationOn

trait DiffCompanionMacro extends DiffMagnoliaDerivation {
  given fallback[T](using DiffAutoDerivationOn): Derived[Diff[T]] = Derived(Diff.useEquals[T])
}
