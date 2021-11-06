package com.softwaremill.diffx.instances

import com.softwaremill.diffx.{Diff, DiffContext, DiffResult, DiffResultValue, ModifyPath}

private[diffx] class DiffForEither[L, R](ld: Diff[L], rd: Diff[R]) extends Diff[Either[L, R]] {
  override def apply(
      left: Either[L, R],
      right: Either[L, R],
      context: DiffContext
  ): DiffResult = {
    (left, right) match {
      case (Left(v1), Left(v2)) =>
        ld.apply(v1, v2, context.getNextStep(ModifyPath.Subtype("scala.package", "Left")))
      case (Right(v1), Right(v2)) =>
        rd.apply(v1, v2, context.getNextStep(ModifyPath.Subtype("scala.package", "Right")))
      case (v1, v2) => DiffResultValue(v1, v2)
    }
  }
}
