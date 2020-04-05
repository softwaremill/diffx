package com.softwaremill.diffx.utest

import com.softwaremill.diffx.{Diff, DiffResultDifferent}
import utest.AssertionError

trait DiffxSupport {

  def assertEqual[T: Diff](t1: T, t2: T): Unit = {
    val result = Diff.compare(t1, t2)
    result match {
      case different: DiffResultDifferent => throw AssertionError(different.show, Seq.empty, null)
      case _                              => // do nothing
    }
  }
}

object DiffxSupport extends DiffxSupport
