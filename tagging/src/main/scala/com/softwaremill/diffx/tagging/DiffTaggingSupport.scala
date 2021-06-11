package com.softwaremill.diffx.tagging

import com.softwaremill.diffx.Diff
import com.softwaremill.tagging.@@

trait DiffTaggingSupport {
  implicit def taggedDiff[T: Diff, U]: Diff[T @@ U] = Diff[T].contramap[T @@ U](identity)
}
