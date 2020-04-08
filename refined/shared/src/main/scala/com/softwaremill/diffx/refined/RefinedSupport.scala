package com.softwaremill.diffx.refined

import com.softwaremill.diffx.{Derived, Diff}
import eu.timepit.refined.api.Refined

trait RefinedSupport {
  implicit def refinedDiff[T: Diff, P]: Derived[Diff[T Refined P]] = Derived(Diff[T].contramap[T Refined P](_.value))
}
