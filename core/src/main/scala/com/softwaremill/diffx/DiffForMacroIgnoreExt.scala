package com.softwaremill.diffx

trait DiffForMacroIgnoreExt {
  implicit class IgnoreMacroExt[T](t: DiffFor[T]) {
    def ignore[U](path: T => U): DiffFor[T] = macro IgnoreMacro.ignoreMacro[T, U]
  }
}
