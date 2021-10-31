package com.softwaremill.diffx

import com.softwaremill.diffx.generic.DiffMagnoliaDerivation
import magnolia.Magnolia

trait DiffCompanionMacro extends DiffMagnoliaDerivation {
  def derived[T]: Diff[T] = macro Magnolia.gen[T]
}
