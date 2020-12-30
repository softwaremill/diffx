package com.softwaremill.diffx.instances

import com.softwaremill.diffx._

private[diffx] class DiffForOption[T](dt: Diff[T]) extends Diff[Option[T]] {
  override def apply(left: Option[T], right: Option[T], toIgnore: List[FieldPath]): DiffResult = {
    (left, right) match {
      case (Some(l), Some(r)) => dt.apply(l, r, toIgnore)
      case (None, None)       => Identical(None)
      case (l, r)             => DiffResultValue(l, r)
    }
  }
}
