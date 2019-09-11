package com.softwaremill.diffx

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

object IgnoreMacro {
  private val ShapeInfo = "Path must have shape: _.field1.field2.each.field3.(...)"

  def ignoreMacro[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[T => U]): c.Tree = applyIgnored(c)(ignoredFromPathMacro(c)(path))

  def ignoreMacroDerived[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[T => U]): c.Tree = applyIgnoredDerived(c)(ignoredFromPathMacro(c)(path))

  private def applyIgnored[T: c.WeakTypeTag, U: c.WeakTypeTag](c: blackbox.Context)(
      path: c.Expr[List[String]]
  ): c.Tree = {
    import c.universe._

    val wrappedValue = c.macroApplication match {
      case Apply(TypeApply(Select(Apply(_, List(w)), _), _), _) => w
      case _                                                    => c.abort(c.enclosingPosition, s"Unknown usage of IgnoreMacroExt. Please file a bug.")
    }

    val valueAlias = TermName(c.freshName())

    q"""{
      val $valueAlias = $wrappedValue;
      ${Ident(valueAlias)}.ignoreUnsafe($path:_*)
     }"""
  }

  private def applyIgnoredDerived[T: c.WeakTypeTag, U: c.WeakTypeTag](c: blackbox.Context)(
      path: c.Expr[List[String]]
  ): c.Tree = {
    import c.universe._

    val wrappedValue = c.macroApplication match {
      case Apply(TypeApply(Select(Apply(_, List(w)), _), _), _) => w
      case _                                                    => c.abort(c.enclosingPosition, s"Unknown usage of IgnoreMacroExt. Please file a bug.")
    }

    val valueAlias = TermName(c.freshName())

    q"""{
      val $valueAlias = $wrappedValue;
      ${Ident(valueAlias)}.value.ignoreUnsafe($path:_*)
     }"""
  }

  /**
    * Converts path to list of strings
    */
  def ignoredFromPathMacro[T: c.WeakTypeTag, U: c.WeakTypeTag](c: blackbox.Context)(
      path: c.Expr[T => U]
  ): c.Expr[List[String]] = {
    import c.universe._

    sealed trait PathElement
    case class TermPathElement(term: c.TermName, xargs: c.Tree*) extends PathElement
    case class FunctorPathElement(functor: c.Tree, method: c.TermName, xargs: c.Tree*) extends PathElement

    /**
      * _.a.b.each.c => List(TPE(a), TPE(b), FPE(functor, each/at/eachWhere, xargs), TPE(c))
      */
    @tailrec
    def collectPathElements(tree: c.Tree, acc: List[PathElement]): List[PathElement] = {
      def typeSupported(quicklensType: c.Tree) =
        Seq("DiffxEach", "DiffxAt", "DiffxMapAt", "DiffxWhen", "DiffxEither", "DiffxEachMap")
          .exists(quicklensType.toString.endsWith)

      tree match {
        case q"$parent.$child " =>
          collectPathElements(parent, TermPathElement(child) :: acc)
        case q"$tpname[..$_]($t)($f) " if typeSupported(tpname) =>
          val newAcc = acc match {
            // replace the term controlled by quicklens
            case TermPathElement(term, xargs @ _*) :: rest => FunctorPathElement(f, term, xargs: _*) :: rest
            case pathEl :: _ =>
              c.abort(c.enclosingPosition, s"Invalid use of path element $pathEl. $ShapeInfo, got: ${path.tree}")
          }
          collectPathElements(t, newAcc)
        case t: Ident => acc
        case _        => c.abort(c.enclosingPosition, s"Unsupported path element. $ShapeInfo, got: $tree")
      }
    }

    val pathEls = path.tree match {
      case q"($arg) => $pathBody " => collectPathElements(pathBody, Nil)
      case _                       => c.abort(c.enclosingPosition, s"$ShapeInfo, got: ${path.tree}")
    }

    c.Expr[List[String]](q"${pathEls.collect {
      case TermPathElement(c) => c.decodedName.toString
    }}")
  }

  private[diffx] def ignoredFromPath[T, U](path: T => U): List[String] = macro ignoredFromPathMacro[T, U]
}
