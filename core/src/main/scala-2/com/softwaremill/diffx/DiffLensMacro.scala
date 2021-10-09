package com.softwaremill.diffx

trait DiffLensMacro[T, U] {
  def useMatcher[M](matcher: ObjectMatcher[M]): Diff[T] = macro ModifyMacro.withObjectMatcher[T, U, M]
}
