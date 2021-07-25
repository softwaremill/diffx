package com.softwaremill.diffx

trait DiffMacros[-T] { outer: Diff[T] =>
  inline def modify[S<:T,U](inline path: S => U): DiffLens[S, U] = ${ ModifyMacro.modifyMacro[S, U]('{outer.asInstanceOf[Diff[S]]})('path) }
  inline def ignore[S<:T,U](inline path: S => U): Diff[S] = ${ ModifyMacro.ignoreMacro[S, U]('{outer.asInstanceOf[Diff[S]]})('path) }
}

