package asserts.diff

import scala.reflect.macros.blackbox

object ProxyDerivationDispatcher {

  def impl[T: c.WeakTypeTag](c: blackbox.Context): c.universe.Tree = {
    import c.universe._
    val symbol = c.symbolOf[T]
    val t = weakTypeOf[T]
    if (symbol.isClass && (symbol.asClass.isCaseClass || symbol.asClass.isSealed)) {
      q"gen[$t]"
    } else {
      q"""new asserts.diff.DiffFor[$t] {
      override def diff(left: $t, right: $t): asserts.diff.DiffResult = {
        if (left.toString != right.toString) {
          asserts.diff.DiffResultValue(left, right)
        } else {
         asserts.diff.Identical(left)
        }
      }
    }"""
    }
  }
}

trait DiffForAnyDerivation extends DiffForMagnoliaDerivation {
  implicit def diffForAny[T]: DiffFor[T] = macro ProxyDerivationDispatcher.impl[T]
}
