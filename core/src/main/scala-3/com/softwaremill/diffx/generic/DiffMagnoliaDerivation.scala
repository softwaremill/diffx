package com.softwaremill.diffx.generic

import com.softwaremill.diffx.{
  Derived,
  Diff,
  DiffContext,
  DiffResult,
  DiffResultObject,
  DiffResultValue,
  ModifyPath,
  nullGuard
}

import scala.collection.mutable
import magnolia1.*

import scala.collection.immutable.ListMap
import scala.deriving.Mirror

trait DiffMagnoliaDerivation extends Derivation[Diff] {

  def join[T](ctx: CaseClass[Diff, T]): Diff[T] = {
    if (!ctx.isValueClass) {
      new Diff[T] {
        override def apply(left: T, right: T, context: DiffContext): DiffResult = {
          nullGuard(left, right) { (left, right) =>
            val map = ListMap(ctx.params.map { p =>
              val lType = p.deref(left)
              val pType = p.deref(right)
              val fieldDiffMod =
                context
                  .getOverride(ModifyPath.Field(p.label))
                  .map(_.asInstanceOf[Diff[p.PType] => Diff[p.PType]])
                  .getOrElse(identity[Diff[p.PType]] _)
              val fieldDiff = fieldDiffMod(p.typeclass)
              p.label -> fieldDiff(lType, pType, context.getNextStep(ModifyPath.Field(p.label)))
            }: _*)
            DiffResultObject(ctx.typeInfo.short, map)
          }
        }
      }
    } else {
      Diff.useEquals[T]
    }
  }

  override def split[T](ctx: SealedTrait[Diff, T]): Diff[T] = new Diff[T] {
    override def apply(left: T, right: T, context: DiffContext): DiffResult = {
      nullGuard(left, right) { (left, right) =>
        val lType = ctx.choose(left)(a => a)
        val rType = ctx.choose(right)(a => a)
        if (lType.typeInfo == rType.typeInfo) {
          val leftTypeClass = lType.typeclass
          val contextPath = ModifyPath.Subtype(lType.typeInfo.owner, lType.typeInfo.short)
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
          DiffResultValue(lType.typeInfo.full, rType.typeInfo.full)
        }
      }
    }
  }
}
