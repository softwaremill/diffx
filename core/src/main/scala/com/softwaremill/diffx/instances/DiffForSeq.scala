package com.softwaremill.diffx.instances

import com.softwaremill.diffx.ObjectMatcher.{SeqEntry, MapEntry}
import com.softwaremill.diffx._
import com.softwaremill.diffx.instances.DiffForSeq._
import com.softwaremill.diffx.instances.internal.MatchResult

import scala.annotation.tailrec
import scala.collection.immutable.ListMap

class DiffForSeq[C[_], T](
    dt: Diff[T],
    matcher: ObjectMatcher[SeqEntry[T]],
    seqLike: SeqLike[C],
    typeName: String = "List"
) extends Diff[C[T]] {
  override def apply(left: C[T], right: C[T], context: DiffContext): DiffResult = nullGuard(left, right) {
    (left, right) =>
      val adjustedMatcher = context.getMatcherOverride[SeqEntry[T]].getOrElse(matcher)
      val leftWithIndex = seqLike.asSeq(left).zipWithIndex.map { case (lv, i) => MapEntry(i, lv) }
      val rightWithIndex = seqLike.asSeq(right).zipWithIndex.map { case (rv, i) => MapEntry(i, rv) }

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
      DiffResultObject(typeName, diffs)
  }

  @tailrec
  private def insertUnmatchedLeft(
      matches: Seq[MatchResult[SeqEntry[T]]],
      item: MatchResult.UnmatchedLeft[SeqEntry[T]],
      bigHead: Seq[MatchResult[SeqEntry[T]]]
  ): Seq[MatchResult[SeqEntry[T]]] = {
    matches.toList match {
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
      left: Seq[SeqEntry[T]],
      right: Seq[SeqEntry[T]],
      matcher: ObjectMatcher[SeqEntry[T]],
      matched: List[MatchResult[SeqEntry[T]]],
      context: DiffContext
  ): (Seq[MatchResult[SeqEntry[T]]], Seq[MatchResult.UnmatchedLeft[SeqEntry[T]]]) = {
    right.toList match {
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

object DiffForSeq {
  implicit def iterableEntryOrdering[T]: Ordering[SeqEntry[T]] = Ordering.by(_.key)

  implicit def diffResultOrdering[T]: Ordering[MatchResult[SeqEntry[T]]] =
    new Ordering[MatchResult[SeqEntry[T]]] {
      override def compare(x: MatchResult[SeqEntry[T]], y: MatchResult[SeqEntry[T]]): Int = {
        (x, y) match {
          case (ur: MatchResult.UnmatchedRight[SeqEntry[T]], m: MatchResult.Matched[SeqEntry[T]]) =>
            iterableEntryOrdering.compare(ur.v, m.r)
          case (m: MatchResult.Matched[SeqEntry[T]], ur: MatchResult.UnmatchedRight[SeqEntry[T]]) =>
            iterableEntryOrdering.compare(m.r, ur.v)
          case (ur: MatchResult.UnmatchedRight[SeqEntry[T]], ur2: MatchResult.UnmatchedRight[SeqEntry[T]]) =>
            iterableEntryOrdering.compare(ur.v, ur2.v)
          case (m1: MatchResult.Matched[SeqEntry[T]], m2: MatchResult.Matched[SeqEntry[T]]) =>
            Ordering
              .by[MatchResult.Matched[SeqEntry[T]], (SeqEntry[T], SeqEntry[T])](m => (m.r, m.l))
              .compare(m1, m2)
          case (_: MatchResult.UnmatchedLeft[_], _) => throw new IllegalStateException("cannot happen")
          case (_, _: MatchResult.UnmatchedLeft[_]) => throw new IllegalStateException("cannot happen")
        }
      }
    }
}
