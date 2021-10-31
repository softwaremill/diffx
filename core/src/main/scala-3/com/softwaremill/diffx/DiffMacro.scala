package com.softwaremill.diffx

trait DiffMacro[T] { outer: Diff[T] =>
  inline def modify[U](inline path: T => U): DiffLens[T, U] = ${ ModifyMacro.modifyMacro[T, U]('outer)('path) }
  inline def ignore[U](inline path: T => U)(implicit config: DiffConfiguration): Diff[T] = ${
    ModifyMacro.ignoreMacro[T, U]('outer)('path, 'config)
  }
}
