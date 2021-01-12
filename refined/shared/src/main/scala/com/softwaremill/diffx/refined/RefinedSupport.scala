package com.softwaremill.diffx.refined

import com.softwaremill.diffx.Diff
import eu.timepit.refined.api.Refined

trait RefinedSupport {
  implicit def refinedDiff[T: Diff, P]: Diff[T Refined P] = Diff[T].contramap[T Refined P](_.value)
}
