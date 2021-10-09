package com.softwaremill.diffx

trait DiffMacro[-T] { outer: Diff[T] =>
  inline def modify[S<:T,U](inline path: S => U): DiffLens[S, U] = ${ ModifyMacro.modifyMacro[S, U]('{outer.asInstanceOf[Diff[S]]})('path) }
  inline def ignore[S<:T,U](inline path: S => U)(implicit config: DiffConfiguration): Diff[S] = ${ ModifyMacro.ignoreMacro[S, U]('{outer.asInstanceOf[Diff[S]]})('path, 'config) }
}

