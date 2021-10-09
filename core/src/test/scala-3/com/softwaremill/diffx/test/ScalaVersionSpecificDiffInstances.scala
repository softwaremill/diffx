package com.softwaremill.diffx.test

import com.softwaremill.diffx.{Derived, Diff}
import com.softwaremill.diffx.generic.auto.diffForCaseClass

trait ScalaVersionSpecificDiffInstances {
  given diffPerson: Derived[Diff[Person]] = diffForCaseClass[Person]
  given diffFamily: Derived[Diff[Family]] = diffForCaseClass[Family]
  given diffOrg : Derived[Diff[Organization]] = diffForCaseClass[Organization]
  given diffDirectionIn: Derived[Diff[TsDirection.Incoming.type]] = diffForCaseClass[TsDirection.Incoming.type]
  given diffDirectionOut: Derived[Diff[TsDirection.Outgoing.type]] = diffForCaseClass[TsDirection.Outgoing.type]
  given diffDirection: Derived[Diff[TsDirection]] = diffForCaseClass[TsDirection]
  given diffStartup: Derived[Diff[Startup]] = diffForCaseClass[Startup]
  given diffKeyModel : Derived[Diff[KeyModel]] = diffForCaseClass[KeyModel]
  given diffMyLookup: Derived[Diff[MyLookup]] = diffForCaseClass[MyLookup]
}
