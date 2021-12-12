package com.softwaremill.diffx.munit

import com.softwaremill.diffx.{ShowConfig, Diff}
import munit.Assertions._
import munit.Location

trait DiffxAssertions {
  def assertEqual[T: Diff](t1: T, t2: T)(implicit c: ShowConfig, loc: Location): Unit = {
    val result = Diff.compare(t1, t2)
    if (!result.isIdentical) {
      fail(result.show())(loc)
    }
  }
}

object DiffxAssertions extends DiffxAssertions
