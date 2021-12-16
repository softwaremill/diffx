package com.softwaremill.diffx

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

object ModifyMacro {
  private val ShapeInfo = "Path must have shape: _.field1.field2.each.field3.(...)"
  private val SubtypeShapeInfo = "Path must have shape: _.subtype[T].field1.(...)"

  def ignoreMacro[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[T => U])(conf: c.Expr[DiffConfiguration]): c.Tree =
    applyIgnored[T, U](c)(modifiedFromPathMacro(c)(path), conf)

  private def applyIgnored[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[List[ModifyPath]], conf: c.Expr[DiffConfiguration]): c.Tree = {
    import c.universe._
    val lens = applyModified[T, U](c)(path)
    q"""$lens.ignore($conf)"""
  }

  def modifyMacro[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[T => U]): c.Tree = applyModified[T, U](c)(modifiedFromPathMacro(c)(path))

  private def applyModified[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[List[ModifyPath]]): c.Tree = {
    import c.universe._
    q"""{
      com.softwaremill.diffx.DiffLens(${c.prefix}, $path)
     }"""
  }

  /** Converts path to list of strings
    */
  def modifiedFromPathMacro[T: c.WeakTypeTag, U: c.WeakTypeTag](c: blackbox.Context)(
      path: c.Expr[T => U]
  ): c.Expr[List[ModifyPath]] = {
    import c.universe._

    sealed trait PathElement
    case class TermPathElement(term: c.TermName, xargs: c.Tree*) extends PathElement
    case class FunctorPathElement(functor: c.Tree, method: c.TermName, xargs: c.Tree*) extends PathElement
    case class SubtypePathElement(subtype: c.Symbol) extends PathElement

    /** _.a.b.each.c => List(TPE(a), TPE(b), FPE(functor, each/at/eachWhere, xargs), TPE(c))
      */
    @tailrec
    def collectPathElements(tree: c.Tree, acc: List[PathElement]): List[PathElement] = {
      def typeSupported(diffxIgnoreType: c.Tree) =
        Seq("DiffxEach", "DiffxEither", "DiffxEachMap", "toSubtypeSelector")
          .exists(diffxIgnoreType.toString.endsWith)

      tree match {
        case q"$parent.$child " =>
          collectPathElements(parent, TermPathElement(child) :: acc)
        case q"$tpname[$supertype]($rest).subtype[$tp]" if typeSupported(tpname) =>
          //TODO check if this is indeed a subtype
          acc match {
            case (_: TermPathElement) :: _ => // do nothing
            case pathEl :: _ =>
              c.abort(c.enclosingPosition, s"Invalid use of subtype element $pathEl. $SubtypeShapeInfo, got: ${path.tree}")
            case Nil =>
              c.abort(c.enclosingPosition, s"Invalid use of subtype element(Nil). $SubtypeShapeInfo, got: ${path.tree}")
          }
          collectPathElements(rest, SubtypePathElement(tp.tpe.typeSymbol) :: acc)
        case q"$tpname[..$_]($t)($f)" if typeSupported(tpname) =>
          val newAcc = acc match {
            // replace the term controlled by quicklens
            case TermPathElement(term, xargs @ _*) :: rest => FunctorPathElement(f, term, xargs: _*) :: rest
            case pathEl :: _ =>
              c.abort(c.enclosingPosition, s"Invalid use of path element $pathEl. $ShapeInfo, got: ${path.tree}")
            case Nil =>
              c.abort(c.enclosingPosition, s"Invalid use of path element(Nil). $ShapeInfo, got: ${path.tree}")
          }
          collectPathElements(t, newAcc)
        case _: Ident => acc
        case _ =>
          c.abort(c.enclosingPosition, s"Unsupported path element. $ShapeInfo, got: $tree")
      }
    }

    val pathEls = path.tree match {
      case q"($arg) => $pathBody " => collectPathElements(pathBody, Nil)
      case _                       => c.abort(c.enclosingPosition, s"$ShapeInfo, got: ${path.tree}")
    }

    def makeSubtype(symbol: c.Symbol): Tree = {
      q"_root_.com.softwaremill.diffx.ModifyPath.Subtype(${symbol.owner.fullName}, ${symbol.name.decodedName.toString})"
    }

    c.Expr[List[ModifyPath]](
      q"${pathEls.collect {
        case TermPathElement(c) => q"_root_.com.softwaremill.diffx.ModifyPath.Field(${c.decodedName.toString})"
        case FunctorPathElement(_, method, _ @_*) if method.decodedName.toString == "eachLeft" =>
          makeSubtype(symbolOf[Left[Any, Any]])
        case FunctorPathElement(_, method, _ @_*) if method.decodedName.toString == "eachRight" =>
          makeSubtype(symbolOf[Right[Any, Any]])
        case FunctorPathElement(_, method, _ @_*) if method.decodedName.toString == "each" =>
          q"_root_.com.softwaremill.diffx.ModifyPath.Each"
        case FunctorPathElement(_, method, _ @_*) if method.decodedName.toString == "eachKey" =>
          q"_root_.com.softwaremill.diffx.ModifyPath.EachKey"
        case FunctorPathElement(_, method, _ @_*) if method.decodedName.toString == "eachValue" =>
          q"_root_.com.softwaremill.diffx.ModifyPath.EachValue"
        case SubtypePathElement(subtype) =>
          makeSubtype(subtype)
      }}"
    )
  }

  private[diffx] def modifiedFromPath[T, U](path: T => U): List[ModifyPath] =
    macro modifiedFromPathMacro[T, U]
}
