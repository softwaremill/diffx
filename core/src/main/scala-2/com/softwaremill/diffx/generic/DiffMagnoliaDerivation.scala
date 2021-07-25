package com.softwaremill.diffx.generic

import com.softwaremill.diffx.{
  Diff,
  DiffContext,
  DiffResultObject,
  DiffResultValue,
  FieldPath,
  IdenticalValue,
  nullGuard
}
import magnolia._

import scala.collection.immutable.ListMap

trait DiffMagnoliaDerivation extends LowPriority {
  type Typeclass[T] = Diff[T]

  def combine[T](ctx: ReadOnlyCaseClass[Typeclass, T]): Diff[T] = { (left: T, right: T, context: DiffContext) =>
    nullGuard(left, right) { (left, right) =>
      val map = ListMap(ctx.parameters.map { p =>
        val lType = p.dereference(left)
        val pType = p.dereference(right)
        val fieldDiff = context.getOverride(p.label).map(_.asInstanceOf[Diff[p.PType]]).getOrElse(p.typeclass)
        p.label -> fieldDiff(lType, pType, context.getNextStep(p.label))
      }: _*)
      DiffResultObject(ctx.typeName.short, map)
    }
  }

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Diff[T] = { (left: T, right: T, context: DiffContext) =>
    nullGuard(left, right) { (left, right) =>
      val lType = ctx.dispatch(left)(a => a)
      val rType = ctx.dispatch(right)(a => a)
      if (lType == rType) {
        lType.typeclass(lType.cast(left), lType.cast(right), context)
      } else {
        DiffResultValue(lType.typeName.full, rType.typeName.full)
      }
    }
  }
}

trait LowPriority {
  def fallback[T]: Diff[T] = Diff.useEquals
}
