package com.softwaremill.diffx.instances

import com.softwaremill.diffx.ObjectMatcher.{IterableEntry, MapEntry}
import com.softwaremill.diffx._
import com.softwaremill.diffx.instances.DiffForIterable._
import com.softwaremill.diffx.instances.internal.MatchResult

import scala.annotation.tailrec
import scala.collection.immutable.ListMap

private[diffx] class DiffForIterable[T, C[W] <: Iterable[W]](
    dt: Diff[T],
    matcher: ObjectMatcher[IterableEntry[T]]
) extends Diff[C[T]] {
  override def apply(left: C[T], right: C[T], context: DiffContext): DiffResult = nullGuard(left, right) {
    (left, right) =>
      val adjustedMatcher = context.getMatcherOverride[IterableEntry[T]].getOrElse(matcher)
      val leftWithIndex = left.zipWithIndex.map { case (lv, i) => MapEntry(i, lv) }.toList
      val rightWithIndex = right.zipWithIndex.map { case (rv, i) => MapEntry(i, rv) }.toList

      val (matched, unmatched) = matchPairs(leftWithIndex, rightWithIndex, adjustedMatcher, List.empty, context)

      val allMatches = unmatched
        .foldLeft(matched.sorted(diffResultOrdering[T].reverse)) { (acc, item) =>
          insertUnmatchedLeft(acc, item, Nil)
        }
        .reverse

      val rawDiffs = allMatches.map {
        case MatchResult.UnmatchedLeft(v)  => DiffResultAdditional(v.value)
        case MatchResult.UnmatchedRight(v) => DiffResultMissing(v.value)
        case MatchResult.Matched(l, r)     => dt.apply(l.value, r.value, context)
      }
      val reindexed = rawDiffs.zipWithIndex.map(_.swap)
      val diffs = ListMap(reindexed.map { case (k, v) => k.toString -> v }: _*)
      DiffResultObject("List", diffs)
  }

  @tailrec
  private def insertUnmatchedLeft(
      matches: List[MatchResult[IterableEntry[T]]],
      item: MatchResult.UnmatchedLeft[IterableEntry[T]],
      bigHead: List[MatchResult[IterableEntry[T]]]
  ): List[MatchResult[IterableEntry[T]]] = {
    matches match {
      case ::(head, tl) =>
        val shouldBeAfter = head match {
          case MatchResult.UnmatchedRight(_) => true
          case MatchResult.UnmatchedLeft(v)  => v.key > item.v.key
          case MatchResult.Matched(l, _)     => l.key > item.v.key
        }
        if (shouldBeAfter) {
          insertUnmatchedLeft(tl, item, bigHead :+ head)
        } else {
          bigHead ++ (item :: (head :: tl))
        }
      case Nil => bigHead :+ item
    }
  }

  @tailrec
  private def matchPairs(
      left: List[IterableEntry[T]],
      right: List[IterableEntry[T]],
      matcher: ObjectMatcher[IterableEntry[T]],
      matched: List[MatchResult[IterableEntry[T]]],
      context: DiffContext
  ): (List[MatchResult[IterableEntry[T]]], List[MatchResult.UnmatchedLeft[IterableEntry[T]]]) = {
    right match {
      case ::(rHead, tailRight) =>
        val maybeMatched = left
          .collect { case l if matcher.isSameObject(rHead, l) => l -> rHead }
          .sortBy { case (l, r) => !dt.apply(l.value, r.value, context).isIdentical }
          .headOption
        maybeMatched match {
          case Some((lm, rm)) =>
            matchPairs(
              left.filterNot(l => l.key == lm.key),
              tailRight,
              matcher,
              matched :+ MatchResult.Matched(lm, rm),
              context
            )
          case None => matchPairs(left, tailRight, matcher, matched :+ MatchResult.UnmatchedRight(rHead), context)
        }
      case Nil => matched -> left.map(l => MatchResult.UnmatchedLeft(l))
    }
  }
}

object DiffForIterable {
  implicit def iterableEntryOrdering[T]: Ordering[IterableEntry[T]] = Ordering.by(_.key)

  implicit def diffResultOrdering[T]: Ordering[MatchResult[IterableEntry[T]]] =
    new Ordering[MatchResult[IterableEntry[T]]] {
      override def compare(x: MatchResult[IterableEntry[T]], y: MatchResult[IterableEntry[T]]): Int = {
        (x, y) match {
          case (ur: MatchResult.UnmatchedRight[IterableEntry[T]], m: MatchResult.Matched[IterableEntry[T]]) =>
            iterableEntryOrdering.compare(ur.v, m.r)
          case (m: MatchResult.Matched[IterableEntry[T]], ur: MatchResult.UnmatchedRight[IterableEntry[T]]) =>
            iterableEntryOrdering.compare(m.r, ur.v)
          case (ur: MatchResult.UnmatchedRight[IterableEntry[T]], ur2: MatchResult.UnmatchedRight[IterableEntry[T]]) =>
            iterableEntryOrdering.compare(ur.v, ur2.v)
          case (m1: MatchResult.Matched[IterableEntry[T]], m2: MatchResult.Matched[IterableEntry[T]]) =>
            Ordering
              .by[MatchResult.Matched[IterableEntry[T]], (IterableEntry[T], IterableEntry[T])](m => (m.r, m.l))
              .compare(m1, m2)
          case (_: MatchResult.UnmatchedLeft[_], _) => throw new IllegalStateException("cannot happen")
          case (_, _: MatchResult.UnmatchedLeft[_]) => throw new IllegalStateException("cannot happen")
        }
      }
    }
}
