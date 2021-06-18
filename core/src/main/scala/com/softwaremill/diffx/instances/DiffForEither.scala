package com.softwaremill.diffx.instances

import com.softwaremill.diffx.{Diff, DiffContext, DiffResult, DiffResultValue}

private[diffx] class DiffForEither[L, R](ld: Diff[L], rd: Diff[R]) extends Diff[Either[L, R]] {
  override def apply(
      left: Either[L, R],
      right: Either[L, R],
      context: DiffContext
  ): DiffResult = {
    (left, right) match {
      case (Left(v1), Left(v2))   => ld.apply(v1, v2, context.getNextStep("eachLeft"))
      case (Right(v1), Right(v2)) => rd.apply(v1, v2, context.getNextStep("eachRight"))
      case (v1, v2)               => DiffResultValue(v1, v2)
    }
  }
}
