package com.softwaremill.diffx

import magnolia._

import scala.language.experimental.macros

trait DiffForMagnoliaDerivation extends LowPriority {
  type Typeclass[T] = DiffFor[T]

  def combine[T](ctx: CaseClass[DiffFor, T]): DiffFor[T] = new Typeclass[T] {
    override def apply(left: T, right: T, toIgnore: List[List[String]]): DiffResult = {
      val map = ctx.parameters.map { p =>
        val lType = p.dereference(left)
        val pType = p.dereference(right)
        if (toIgnore.exists(_.contains(p.label))) {
          p.label -> Identical(lType)
        } else {
          p.label -> p.typeclass(lType, pType, toIgnore.map(_.drop(1)))
        }
      }.toMap
      if (map.filterKeys(k => !toIgnore.exists(_.contains(k))).values.forall(p => p.isIdentical)) {
        Identical(left)
      } else {
        DiffResultObject(ctx.typeName.short, map)
      }
    }
  }

  def dispatch[T](ctx: SealedTrait[DiffFor, T]): DiffFor[T] = { (left: T, right: T, toIgnore: List[List[String]]) =>
    {
      val lType = ctx.dispatch(left)(a => a)
      val rType = ctx.dispatch(right)(a => a)
      if (lType == rType) {
        lType.typeclass(lType.cast(left), lType.cast(right))
      } else {
        DiffResultValue(lType.typeName.full, rType.typeName.full)
      }
    }
  }

  implicit def gen[T]: DiffFor[T] = macro Magnolia.gen[T]
}

trait LowPriority {
  def fallback[T]: DiffFor[T] = (left: T, right: T, toIgnore: List[List[String]]) => {
    if (left != right) {
      DiffResultValue(left, right)
    } else {
      Identical(left)
    }
  }
}
