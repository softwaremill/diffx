package com.softwaremill.diffx
import acyclic.skipped

private[diffx] object Matching {
  private[diffx] def matching[T](
      left: Set[T],
      right: Set[T],
      matcher: ObjectMatcher[T],
      diff: Diff[T],
      toIgnore: List[FieldPath]
  ): MatchingResults[T] = {
    val matchedKeys = left.flatMap(
      l =>
        right.collectFirst {
          case r if matcher.isSameObject(l, r) || diff(l, r, toIgnore).isIdentical => l -> r
        }
    )
    MatchingResults(left.diff(matchedKeys.map(_._1)), right.diff(matchedKeys.map(_._2)), matchedKeys)
  }

  private[diffx] case class MatchingResults[T](unmatchedLeft: Set[T], unmatchedRight: Set[T], matched: Set[(T, T)])
}
