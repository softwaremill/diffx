package com.softwaremill.diffx

trait DiffLensMacro[T, U] { outer: DiffLens[T, U] =>
  inline def useMatcher[M](inline matcher: ObjectMatcher[M]): Diff[T] = ${ ModifyMacro.withObjectMatcher[T,U,M]('outer)('matcher) }
}
