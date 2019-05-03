package asserts.diff

import magnolia._

import scala.language.experimental.macros

trait DiffForDerivation extends LowPriorityDiffForInstances {
  type Typeclass[T] = DiffFor[T]

  def combine[T](ctx: CaseClass[DiffFor, T]): DiffFor[T] = (left: T, right: T) => {
    DiffResultObject(ctx.typeName.short, ctx.parameters.map { p =>
      val lType = p.dereference(left)
      val pType = p.dereference(right)
      p.label -> p.typeclass.diff(lType, pType)
    }.toMap)
  }

  def dispatch[T](ctx: SealedTrait[DiffFor, T]): DiffFor[T] =
    throw new RuntimeException(s"Sealed trait hierarchies are not yet supported for: ${ctx.typeName}")

  implicit def gen[T]: DiffFor[T] = macro Magnolia.gen[T]
}
