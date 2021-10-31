package com.softwaremill.diffx

trait DiffMacro[T] {
  def modify[U](path: T => U): DiffLens[T, U] = macro ModifyMacro.modifyMacro[T, U]
  def ignore[U](path: T => U)(implicit conf: DiffConfiguration): Diff[T] = macro ModifyMacro.ignoreMacro[T, U]
}
