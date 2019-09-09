package com.softwaremill.diffx

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

object IgnoreMacro {
  private val ShapeInfo = "Path must have shape: _.field1.field2.each.field3.(...)"

  def ignoreMacro[T: c.WeakTypeTag, U: c.WeakTypeTag](
      c: blackbox.Context
  )(path: c.Expr[T => U]): c.Tree = applyIgnored(c)(ignoredFromPathMacro(c)(path))

  /**
    * A helper method for modifyPimp_impl and modifyAllPimp_impl.
    */
  private def applyIgnored[T: c.WeakTypeTag, U: c.WeakTypeTag](c: blackbox.Context)(
      path: c.Expr[List[String]]
  ): c.Tree = {
    import c.universe._

    val wrappedValue = c.macroApplication match {
      case Apply(TypeApply(Select(Apply(_, List(w)), _), _), _) => w
      case _                                                    => c.abort(c.enclosingPosition, s"Unknown usage of ModifyPimp. Please file a bug.")
    }

    val valueAlias = TermName(c.freshName())

    q"""{
      val $valueAlias = $wrappedValue;
      ${Ident(valueAlias)}.ignoreUnsafe($path:_*)
     }"""
  }

  /**
    * Produce a modification for a single path.
    */
  def ignoredFromPathMacro[T: c.WeakTypeTag, U: c.WeakTypeTag](c: blackbox.Context)(
      path: c.Expr[T => U]
  ): c.Expr[List[String]] = {
    import c.universe._

    sealed trait PathAccess
    case object DirectPathAccess extends PathAccess
    case class SealedPathAccess(types: Set[Symbol]) extends PathAccess

    sealed trait PathElement
    case class TermPathElement(term: c.TermName, access: PathAccess, xargs: c.Tree*) extends PathElement
    //    case class SubtypePathElement(subtype: c.Symbol) extends PathElement
    case class FunctorPathElement(functor: c.Tree, method: c.TermName, xargs: c.Tree*) extends PathElement

    /**
      * Determine if the `.copy` method should be applied directly
      * or through a match across all subclasses (for sealed traits).
      */
    def determinePathAccess(typeSymbol: Symbol) = {
      def ifEmpty[A](set: Set[A], empty: => Set[A]) =
        if (set.isEmpty) empty else set

      def knownDirectSubclasses(sealedSymbol: ClassSymbol) = ifEmpty(
        sealedSymbol.knownDirectSubclasses,
        c.abort(
          c.enclosingPosition,
          s"""Could not find subclasses of sealed trait $sealedSymbol.
             |You might need to ensure that it gets compiled before this invocation.
             |See also: <https://issues.scala-lang.org/browse/SI-7046>.""".stripMargin
        )
      )

      def expand(symbol: Symbol): Set[Symbol] =
        Set(symbol)
          .filter(_.isClass)
          .map(_.asClass)
          .map { s =>
            s.typeSignature; s
          } // see <https://issues.scala-lang.org/browse/SI-7755>
          .filter(_.isSealed)
          .flatMap(s => knownDirectSubclasses(s))
          .flatMap(s => ifEmpty(expand(s), Set(s)))

      val subclasses = expand(typeSymbol)
      if (subclasses.isEmpty) DirectPathAccess else SealedPathAccess(subclasses)
    }

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
          val access = determinePathAccess(parent.tpe.typeSymbol)
          collectPathElements(parent, TermPathElement(child, access) :: acc)
        case q"$tpname[..$_]($t)($f) " if typeSupported(tpname) =>
          val newAcc = acc match {
            // replace the term controlled by quicklens
            case TermPathElement(term, _, xargs @ _*) :: rest => FunctorPathElement(f, term, xargs: _*) :: rest
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
      case TermPathElement(c, _) => c.decodedName.toString
    }}")
  }

  private[diffx] def ignoredFromPath[T, U](path: T => U): List[String] = macro ignoredFromPathMacro[T, U]
}
