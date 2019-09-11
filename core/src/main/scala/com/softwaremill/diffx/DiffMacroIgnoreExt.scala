package com.softwaremill.diffx

trait DiffMacroIgnoreExt {
  implicit class IgnoreMacroExt[T](t: Diff[T]) {
    def ignore[U](path: T => U): Diff[T] = macro IgnoreMacro.ignoreMacro[T, U]
  }
}
