package com.softwaremill.diffx.generic

import com.softwaremill.diffx.Derived
import magnolia.Magnolia

object MagnoliaDerivedMacro {

  import scala.reflect.macros.whitebox

  def derivedGen[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[Derived[T]] = {
    import c.universe._
    c.Expr[Derived[T]](q"com.softwaremill.diffx.Derived(${Magnolia.gen[T](c)(implicitly[c.WeakTypeTag[T]])})")
  }
}
