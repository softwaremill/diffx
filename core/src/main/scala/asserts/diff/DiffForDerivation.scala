package asserts.diff

import magnolia._

import scala.language.experimental.macros

trait DiffForDerivation {
  type Typeclass[T] = DiffFor[T]

  def combine[T](ctx: CaseClass[DiffFor, T]): DiffFor[T] = (left: T, right: T) => {
    DiffResultObject(ctx.typeName.short, ctx.parameters.map { p =>
      val lType = p.dereference(left)
      val pType = p.dereference(right)
      p.label -> p.typeclass.diff(lType, pType)
    }.toMap)
  }

  def dispatch[T](ctx: SealedTrait[DiffFor, T]): DiffFor[T] = { (left: T, right: T) =>
    {
      val lType = ctx.dispatch(left)(a => a)
      val rType = ctx.dispatch(right)(a => a)
      if (lType == rType) {
        lType.typeclass.diff(lType.cast(left), lType.cast(right))
      } else {
        DiffResultValue(lType.typeName.full, rType.typeName.full)
      }
    }
  }

  implicit def gen[T]: DiffFor[T] = macro Magnolia.gen[T]
}
