package com.softwaremill.diffx

trait DerivedDiffLensMacro[T, U] {
  def useMatcher[M](matcher: ObjectMatcher[M]): Derived[Diff[T]] = macro ModifyMacro.withObjectMatcherDerived[T, U, M]
}
