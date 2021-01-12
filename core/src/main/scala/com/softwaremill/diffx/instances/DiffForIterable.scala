package com.softwaremill.diffx.instances

import com.softwaremill.diffx._
import scala.collection.immutable.ListMap

private[diffx] class DiffForIterable[T, C[W] <: Iterable[W]](dot: Diff[Option[T]]) extends Diff[C[T]] {
  override def apply(left: C[T], right: C[T], toIgnore: List[FieldPath]): DiffResult = {
    val indexes = Range(0, Math.max(left.size, right.size))
    val leftAsMap = left.toList.lift
    val rightAsMap = right.toList.lift
    val differences = ListMap(indexes.map { index =>
      index.toString -> (dot.apply(leftAsMap(index), rightAsMap(index), toIgnore) match {
        case DiffResultValue(Some(v), None) => DiffResultAdditional(v)
        case DiffResultValue(None, Some(v)) => DiffResultMissing(v)
        case d                              => d
      })
    }: _*)

    if (differences.values.forall(_.isIdentical)) {
      Identical(left)
    } else {
      DiffResultObject(
        "List",
        differences
      )
    }
  }
}
