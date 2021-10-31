package com.softwaremill.diffx

import com.softwaremill.diffx.ObjectMatcher.{IterableEntry, MapEntry, SetEntry}

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
  )(path: c.Expr[T => U])(conf: c.Expr[DiffConfiguration]): c.Tree =
    applyIgnoredModified[T, U](c)(modifiedFromPathMacro(c)(path), conf)

  private def applyIgnoredModified[T: c.WeakTypeTag, U: c.WeakTypeTag](c: blackbox.Context)(
      path: c.Expr[List[String]],
      conf: c.Expr[DiffConfiguration]
  ): c.Tree = {
    import c.universe._
    val lens = applyDerivedModified[T, U](c)(path)
    q"""$lens.ignore($conf)"""
  }

  def ignoreMacro[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[T => U])(conf: c.Expr[DiffConfiguration]): c.Tree =
    applyIgnored[T, U](c)(modifiedFromPathMacro(c)(path), conf)

  private def applyIgnored[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[List[String]], conf: c.Expr[DiffConfiguration]): c.Tree = {
    import c.universe._
    val lens = applyModified[T, U](c)(path)
    q"""$lens.ignore($conf)"""
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
      q"(${pathEls.collect {
        case TermPathElement(c) => c.decodedName.toString
        case FunctorPathElement(_, method, _ @_*) if method.decodedName.toString == "eachLeft" =>
          method.decodedName.toString
        case FunctorPathElement(_, method, _ @_*) if method.decodedName.toString == "eachRight" =>
          method.decodedName.toString
      }})"
    )
  }

  private[diffx] def modifiedFromPath[T, U](path: T => U): List[String] =
    macro modifiedFromPathMacro[T, U]

  def withObjectMatcherDerived[T: c.WeakTypeTag, U: c.WeakTypeTag, M: c.WeakTypeTag](
      c: blackbox.Context
  )(matcher: c.Expr[ObjectMatcher[M]]): c.Tree = {
    import c.universe._
    val diff = withObjectMatcher[T, U, M](c)(matcher)
    q"com.softwaremill.diffx.Derived($diff)"
  }

  def withObjectMatcher[T: c.WeakTypeTag, U: c.WeakTypeTag, M: c.WeakTypeTag](
      c: blackbox.Context
  )(matcher: c.Expr[ObjectMatcher[M]]): c.Tree = {
    import c.universe._
    val t = weakTypeOf[T]
    val u = weakTypeOf[U]
    val m = weakTypeOf[M]

    val baseIsIterable = u <:< typeOf[Iterable[_]]
    val baseIsSet = u <:< typeOf[Set[_]]
    val baseIsMap = u <:< typeOf[Map[_, _]]
    val typeArgsTheSame = u.typeArgs == m.typeArgs
    val setRequirements = baseIsSet && typeArgsTheSame && m <:< typeOf[SetEntry[_]]
    val iterableRequirements = !baseIsSet && baseIsIterable && typeArgsTheSame && m <:< typeOf[IterableEntry[_]]
    val mapRequirements = baseIsMap && typeArgsTheSame && m <:< typeOf[MapEntry[_, _]]
    if (!setRequirements && !iterableRequirements && !mapRequirements) { //  weakTypeOf[U] <:< tq"Iterable[${u.typeArgs.head.termSymbol}]"
      c.abort(c.enclosingPosition, s"Invalid objectMather type $u for given lens($t,$m)")
    }
    q"""
       val lens = ${c.prefix}
       lens.outer.modifyMatcherUnsafe(lens.path: _*)($matcher)
    """
  }
}
