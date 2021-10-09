package com.softwaremill.diffx.test

import com.softwaremill.diffx.Diff

trait ScalaVersionSpecificDiffInstances {
  given Diff[Person] = Diff.derived[Person]
  given Diff[Family] = Diff.derived[Family]
  given Diff[Organization] = Diff.derived[Organization]
  given Diff[TsDirection.Incoming.type] = Diff.derived[TsDirection.Incoming.type]
  given Diff[TsDirection.Outgoing.type] = Diff.derived[TsDirection.Outgoing.type]
  given Diff[TsDirection] = Diff.derived[TsDirection]
  given Diff[Startup] = Diff.derived[Startup]
  given Diff[KeyModel] = Diff.derived[KeyModel]
  given Diff[MyLookup] = Diff.derived[MyLookup]
}
