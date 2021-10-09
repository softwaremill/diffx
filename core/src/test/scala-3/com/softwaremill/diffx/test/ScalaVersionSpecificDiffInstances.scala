package com.softwaremill.diffx.test

import com.softwaremill.diffx.Diff

trait ScalaVersionSpecificDiffInstances {
  given Diff[Person] = Diff.derived[Person]
  given Diff[Family] = Diff.derived[Family]
  given Diff[Organization] = Diff.derived[Organization]
  given Diff[TsDirection] = Diff.derived[TsDirection]
}
