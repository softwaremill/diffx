package com.softwaremill.diffx.generic

import com.softwaremill.diffx.{Diff, DiffResultObject, DiffResultValue, FieldPath, Identical, nullGuard}
import magnolia._

import scala.collection.immutable.ListMap

trait DiffMagnoliaDerivation extends LowPriority {
  type Typeclass[T] = Diff[T]

  def combine[T](ctx: ReadOnlyCaseClass[Typeclass, T]): Diff[T] = { (left: T, right: T, toIgnore: List[FieldPath]) =>
    nullGuard(left, right) { (left, right) =>
      val map = ListMap(ctx.parameters.map { p =>
        val lType = p.dereference(left)
        val pType = p.dereference(right)
        if (toIgnore.contains(List(p.label))) {
          p.label -> Identical(lType)
        } else {
          val nestedIgnore =
            if (toIgnore.exists(_.headOption.exists(h => h == p.label))) toIgnore.map(_.drop(1)) else Nil
          p.label -> p.typeclass(lType, pType, nestedIgnore)
        }
      }: _*)
      if (map.values.forall(p => p.isIdentical)) {
        Identical(left)
      } else {
        DiffResultObject(ctx.typeName.short, map)
      }
    }
  }

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Diff[T] = { (left: T, right: T, toIgnore: List[FieldPath]) =>
    nullGuard(left, right) { (left, right) =>
      val lType = ctx.dispatch(left)(a => a)
      val rType = ctx.dispatch(right)(a => a)
      if (lType == rType) {
        lType.typeclass(lType.cast(left), lType.cast(right), toIgnore)
      } else {
        DiffResultValue(lType.typeName.full, rType.typeName.full)
      }
    }
  }
}

trait LowPriority {
  def fallback[T]: Diff[T] =
    (left: T, right: T, toIgnore: List[FieldPath]) => {
      if (left != right) {
        DiffResultValue(left, right)
      } else {
        Identical(left)
      }
    }
}
