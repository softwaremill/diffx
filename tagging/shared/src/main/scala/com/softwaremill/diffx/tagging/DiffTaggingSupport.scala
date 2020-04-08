package com.softwaremill.diffx.tagging

import com.softwaremill.diffx.{Derived, Diff}
import com.softwaremill.tagging.@@

trait DiffTaggingSupport {
  implicit def taggedDiff[T: Diff, U]: Derived[Diff[T @@ U]] = Derived(Diff[T].contramap[T @@ U](identity))
}
