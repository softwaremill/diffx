package asserts.diff

import magnolia._

import scala.language.experimental.macros

trait Diff2Derivation {
  type Typeclass[T] = DiffFor[T]

  def combine[T](ctx: CaseClass[DiffFor, T]): DiffFor[T] = (left: T, right: T) => {
    DiffResultObject(ctx.typeName.short, ctx.parameters.map { p =>
      p.label -> p.typeclass.diff(p.dereference(left), p.dereference(right))
    }.toMap)
  }

  def dispatch[T](ctx: SealedTrait[DiffFor, T]): DiffFor[T] =
    throw new RuntimeException(s"Sealed trait hierarchies are not yet supported for: ${ctx.typeName}")

  implicit def gen[T <: Product with Serializable]: DiffFor[T] = macro Magnolia.gen[T]
}
