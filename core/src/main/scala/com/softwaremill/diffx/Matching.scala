package com.softwaremill.diffx

private[diffx] object Matching {
  private[diffx] def matching[T](
      left: scala.collection.Set[T],
      right: scala.collection.Set[T],
      matcher: ObjectMatcher[T],
      diff: Diff[T],
      toIgnore: List[FieldPath]
  ): MatchingResults[T] = {
    val matchedKeys = left.flatMap(l =>
      right.collectFirst {
        case r if matcher.isSameObject(l, r) || diff(l, r, toIgnore).isIdentical => l -> r
      }
    )
    MatchingResults(left.diff(matchedKeys.map(_._1)), right.diff(matchedKeys.map(_._2)), matchedKeys)
  }

  private[diffx] case class MatchingResults[T](
      unmatchedLeft: scala.collection.Set[T],
      unmatchedRight: scala.collection.Set[T],
      matched: scala.collection.Set[(T, T)]
  )
}
