package com.softwaremill.diffx

import magnolia._

import scala.language.experimental.macros

trait DiffForMagnoliaDerivation extends LowPriority {
  type Typeclass[T] = Derived[DiffFor[T]]

  def combine[T](ctx: CaseClass[Typeclass, T]): Derived[DiffFor[T]] =
    Derived(new DiffFor[T] {
      override def apply(left: T, right: T, toIgnore: List[List[String]]): DiffResult = {
        val map = ctx.parameters.map { p =>
          val lType = p.dereference(left)
          val pType = p.dereference(right)
          if (toIgnore.contains(List(p.label))) {
            p.label -> Identical(lType)
          } else {
            val nestedIgnore =
              if (toIgnore.exists(_.headOption.exists(h => h == p.label))) toIgnore.map(_.drop(1)) else Nil
            p.label -> p.typeclass.value(lType, pType, nestedIgnore)
          }
        }.toMap
        if (map.values.forall(p => p.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject(ctx.typeName.short, map)
        }
      }
    })

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Derived[DiffFor[T]] =
    Derived({ (left: T, right: T, toIgnore: List[List[String]]) =>
      {
        val lType = ctx.dispatch(left)(a => a)
        val rType = ctx.dispatch(right)(a => a)
        if (lType == rType) {
          lType.typeclass.value(lType.cast(left), lType.cast(right))
        } else {
          DiffResultValue(lType.typeName.full, rType.typeName.full)
        }
      }
    })

  implicit def gen[T]: Derived[DiffFor[T]] = macro Magnolia.gen[T]
}

trait LowPriority {
  def fallback[T]: Derived[DiffFor[T]] =
    Derived((left: T, right: T, toIgnore: List[List[String]]) => {
      if (left != right) {
        DiffResultValue(left, right)
      } else {
        Identical(left)
      }
    })
}
