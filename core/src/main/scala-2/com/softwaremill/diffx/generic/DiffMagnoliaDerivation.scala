package com.softwaremill.diffx.generic

import com.softwaremill.diffx.{Diff, DiffContext, DiffResultObject, DiffResultValue, ModifyPath, nullGuard}
import magnolia1._

import scala.collection.immutable.ListMap

trait DiffMagnoliaDerivation {
  type Typeclass[T] = Diff[T]

  def join[T](ctx: ReadOnlyCaseClass[Typeclass, T]): Diff[T] = { (left: T, right: T, context: DiffContext) =>
    nullGuard(left, right) { (left, right) =>
      val map = ListMap(ctx.parameters.map { p =>
        val lType = p.dereference(left)
        val pType = p.dereference(right)
        val fieldDiffMod =
          context
            .getOverride(ModifyPath.Field(p.label))
            .map(_.asInstanceOf[Diff[p.PType] => Diff[p.PType]])
            .getOrElse(identity[Diff[p.PType]] _)
        val fieldDiff = fieldDiffMod(p.typeclass)
        p.label -> fieldDiff(lType, pType, context.getNextStep(ModifyPath.Field(p.label)))
      }: _*)
      DiffResultObject(ctx.typeName.short, map)
    }
  }

  def split[T](ctx: SealedTrait[Typeclass, T]): Diff[T] = { (left: T, right: T, context: DiffContext) =>
    nullGuard(left, right) { (left, right) =>
      val lType = ctx.split(left)(a => a)
      val rType = ctx.split(right)(a => a)
      if (lType == rType) {
        val leftTypeClass = lType.typeclass
        val contextPath = ModifyPath.Subtype(lType.typeName.owner, lType.typeName.short)
        val modifyFromOverride = context
          .getOverride(contextPath)
          .map(_.asInstanceOf[leftTypeClass.type => leftTypeClass.type])
          .getOrElse(identity[leftTypeClass.type] _)
        modifyFromOverride(leftTypeClass)(
          lType.cast(left),
          lType.cast(right),
          context.getNextStep(contextPath).merge(context)
        )
      } else {
        DiffResultValue(lType.typeName.full, rType.typeName.full)
      }
    }
  }

  def fallback[T]: Diff[T] = Diff.useEquals[T]
}
