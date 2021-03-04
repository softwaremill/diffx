package com.softwaremill.diffx.instances

import com.softwaremill.diffx.{Diff, DiffResult, DiffResultValue, FieldPath}

private[diffx] class DiffForEither[L, R](ld: Diff[L], rd: Diff[R]) extends Diff[Either[L, R]] {
  override def apply(
      left: Either[L, R],
      right: Either[L, R],
      toIgnore: List[FieldPath]
  ): DiffResult = {
    (left, right) match {
      case (Left(v1), Left(v2))   => ld.apply(v1, v2, toIgnore)
      case (Right(v1), Right(v2)) => rd.apply(v1, v2, toIgnore)
      case (v1, v2)               => DiffResultValue(v1, v2)
    }
  }
}
