package com.softwaremill.diffx.instances

import com.softwaremill.diffx._

private[diffx] class DiffForString extends Diff[String] {
  override def apply(left: String, right: String, toIgnore: List[FieldPath]): DiffResult = nullGuard(left, right) {
    (left, right) =>
      val leftLines = left.split("\n").toList
      val rightLines = right.split("\n").toList
      val leftAsMap = leftLines.lift
      val rightAsMap = rightLines.lift
      val maxSize = Math.max(leftLines.length, rightLines.length)
      val partialResults = (0 until maxSize).map { i =>
        (leftAsMap(i), rightAsMap(i)) match {
          case (Some(lv), Some(rv)) =>
            if (lv == rv) {
              Identical(lv)
            } else {
              DiffResultValue(lv, rv)
            }
          case (Some(lv), None) => DiffResultAdditional(lv)
          case (None, Some(rv)) => DiffResultMissing(rv)
          case (None, None)     => throw new IllegalStateException("That should never happen")
        }
      }.toList
      if (partialResults.forall(_.isIdentical)) {
        Identical(left)
      } else {
        DiffResultString(partialResults)
      }
  }
}
