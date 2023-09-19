package com.softwaremill.diffx.weaver

import com.softwaremill.diffx.{Diff, ShowConfig}
import weaver.Expectations.Helpers.{failure, success}
import weaver.{Expectations, SourceLocation}

trait DiffxExpectations {
  def expectEqual[T: Diff](t1: T, t2: T)(implicit c: ShowConfig, loc: SourceLocation): Expectations = {
    val result = Diff.compare(t1, t2)
    if (result.isIdentical)
      success
    else
      failure(result.show())(loc)
  }
}

object DiffxExpectations extends DiffxExpectations
