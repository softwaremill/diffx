package com.softwaremill.diffx.instances

import com.softwaremill.diffx._

private[diffx] class ApproximateDiffForNumeric[T: Numeric](epsilon: T) extends Diff[T] {
  override def apply(left: T, right: T, context: DiffContext): DiffResult = {
    val numeric = implicitly[Numeric[T]]
    if (numeric.lt(numeric.abs(numeric.minus(left, right)), epsilon)) {
      DiffResultValue(left, right)
    } else {
      Identical(left)
    }
  }
}