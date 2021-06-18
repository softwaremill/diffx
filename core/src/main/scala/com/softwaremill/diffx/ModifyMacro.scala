package com.softwaremill.diffx

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

object ModifyMacro {
  private val ShapeInfo = "Path must have shape: _.field1.field2.each.field3.(...)"

  def derivedModifyMacro[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[T => U]): c.Tree =
    applyDerivedModified[T, U](c)(modifiedFromPathMacro(c)(path))

  private def applyDerivedModified[T: c.WeakTypeTag, U: c.WeakTypeTag](c: blackbox.Context)(
      path: c.Expr[List[String]]
  ): c.Tree = {
    import c.universe._
    q"""com.softwaremill.diffx.DerivedDiffLens(${c.prefix}.dd.value, $path)"""
  }

  def derivedIgnoreMacro[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[T => U]): c.Tree =
    applyIgnoredModified[T, U](c)(modifiedFromPathMacro(c)(path))

  private def applyIgnoredModified[T: c.WeakTypeTag, U: c.WeakTypeTag](c: blackbox.Context)(
      path: c.Expr[List[String]]
  ): c.Tree = {
    import c.universe._
    val lens = applyDerivedModified[T, U](c)(path)
    q"""$lens.ignore()"""
  }

  def ignoreMacro[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[T => U]): c.Tree = applyIgnored[T, U](c)(modifiedFromPathMacro(c)(path))

  private def applyIgnored[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[List[String]]): c.Tree = {
    import c.universe._
    val lens = applyModified[T, U](c)(path)
    q"""$lens.ignore()"""
  }

  def modifyMacro[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[T => U]): c.Tree = applyModified[T, U](c)(modifiedFromPathMacro(c)(path))

  private def applyModified[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[List[String]]): c.Tree = {
    import c.universe._
    q"""{
      com.softwaremill.diffx.DiffLens(${c.prefix}.d, $path)
     }"""
  }

  /** Converts path to list of strings
    */
  def modifiedFromPathMacro[T: c.WeakTypeTag, U: c.WeakTypeTag](c: blackbox.Context)(
      path: c.Expr[T => U]
  ): c.Expr[List[String]] = {
    import c.universe._

    sealed trait PathElement
    case class TermPathElement(term: c.TermName, xargs: c.Tree*) extends PathElement
    case class FunctorPathElement(functor: c.Tree, method: c.TermName, xargs: c.Tree*) extends PathElement

    /** _.a.b.each.c => List(TPE(a), TPE(b), FPE(functor, each/at/eachWhere, xargs), TPE(c))
      */
    @tailrec
    def collectPathElements(tree: c.Tree, acc: List[PathElement]): List[PathElement] = {
      def typeSupported(diffxIgnoreType: c.Tree) =
        Seq("DiffxEach", "DiffxEither", "DiffxEachMap")
          .exists(diffxIgnoreType.toString.endsWith)

      tree match {
        case q"$parent.$child " =>
          collectPathElements(parent, TermPathElement(child) :: acc)
        case q"$tpname[..$_]($t)($f) " if typeSupported(tpname) =>
          val newAcc = acc match {
            // replace the term controlled by quicklens
            case TermPathElement(term, xargs @ _*) :: rest => FunctorPathElement(f, term, xargs: _*) :: rest
            case pathEl :: _ =>
              c.abort(c.enclosingPosition, s"Invalid use of path element $pathEl. $ShapeInfo, got: ${path.tree}")
            case Nil =>
              c.abort(c.enclosingPosition, s"Invalid use of path element(Nil). $ShapeInfo, got: ${path.tree}")
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

    c.Expr[List[String]](
      q"(${pathEls.collect { case TermPathElement(c) =>
        c.decodedName.toString
      }})"
    )
  }

  private[diffx] def modifiedFromPath[T, U](path: T => U): List[String] =
    macro modifiedFromPathMacro[T, U]
}
