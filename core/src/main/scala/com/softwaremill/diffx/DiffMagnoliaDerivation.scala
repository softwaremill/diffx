package com.softwaremill.diffx
import acyclic.skipped
import magnolia._

import scala.collection.immutable.ListMap
import scala.language.experimental.macros

trait DiffMagnoliaDerivation extends LowPriority {
  type Typeclass[T] = Derived[Diff[T]]

  def combine[T](ctx: ReadOnlyCaseClass[Typeclass, T]): Derived[Diff[T]] =
    Derived(new Diff[T] {
      override def apply(left: T, right: T, toIgnore: List[FieldPath]): DiffResult = {
        val map = ListMap(ctx.parameters.map { p =>
          val lType = p.dereference(left)
          val pType = p.dereference(right)
          if (toIgnore.contains(List(p.label))) {
            p.label -> Identical(lType)
          } else {
            val nestedIgnore =
              if (toIgnore.exists(_.headOption.exists(h => h == p.label))) toIgnore.map(_.drop(1)) else Nil
            p.label -> p.typeclass.value(lType, pType, nestedIgnore)
          }
        }: _*)
        if (map.values.forall(p => p.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject(ctx.typeName.short, map)
        }
      }
    })

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Derived[Diff[T]] =
    Derived({ (left: T, right: T, toIgnore: List[FieldPath]) =>
      {
        val lType = ctx.dispatch(left)(a => a)
        val rType = ctx.dispatch(right)(a => a)
        if (lType == rType) {
          lType.typeclass.value(lType.cast(left), lType.cast(right), toIgnore)
        } else {
          DiffResultValue(lType.typeName.full, rType.typeName.full)
        }
      }
    })

  implicit def gen[T]: Derived[Diff[T]] = macro Magnolia.gen[T]
}

trait LowPriority {
  def fallback[T]: Derived[Diff[T]] =
    Derived((left: T, right: T, toIgnore: List[FieldPath]) => {
      if (left != right) {
        DiffResultValue(left, right)
      } else {
        Identical(left)
      }
    })
}
