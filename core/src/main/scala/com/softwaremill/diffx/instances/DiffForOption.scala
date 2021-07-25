package com.softwaremill.diffx.instances

import com.softwaremill.diffx._

private[diffx] class DiffForOption[T](dt: Diff[T]) extends Diff[Option[T]] {
  override def apply(left: Option[T], right: Option[T], context: DiffContext): DiffResult = {
    (left, right) match {
      case (Some(l), Some(r)) => dt.apply(l, r, context)
      case (None, None)       => IdenticalValue(None)
      case (l, r)             => DiffResultValue(l, r)
    }
  }
}
