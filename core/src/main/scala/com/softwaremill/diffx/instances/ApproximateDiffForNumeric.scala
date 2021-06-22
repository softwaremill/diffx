package com.softwaremill.diffx.instances

import com.softwaremill.diffx._

private[diffx] class ApproximateDiffForNumeric[T: Numeric](epsilon: T) extends Diff[T] {
  override def apply(left: T, right: T, context: DiffContext): DiffResult = {
    val numeric = implicitly[Numeric[T]]
    if (numeric.lt(epsilon, numeric.abs(numeric.minus(left, right)))) {
      DiffResultValue(left, right)
    } else {
      IdenticalValue(left)
    }
  }
}
