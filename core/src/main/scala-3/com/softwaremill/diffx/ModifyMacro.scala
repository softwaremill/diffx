package com.softwaremill.diffx

import scala.quoted.*

object ModifyMacro {
  private val ShapeInfo = "Path must have shape: _.field1.field2.each.field3.(...)"

  def ignoreMacro[T: Type, U: Type](base: Expr[Diff[T]])(path: Expr[T => U], conf: Expr[DiffConfiguration])(using Quotes): Expr[Diff[T]] = {
    '{ ${ modifyMacro(base)(path) }.ignore($conf) }
  }

  def modifyMacro[T: Type, U:Type](base: Expr[Diff[T]])(path: Expr[T => U])(using Quotes): Expr[DiffLens[T,U]] = {
    '{
    val p = ${ modifiedFromPathImpl(path) }
    new com.softwaremill.diffx.DiffLens[T,U]($base, p)
    }
  }

  private[diffx] inline def modifiedFromPath[S,U](inline path: S => U): List[String] = ${ ModifyMacro.modifiedFromPathImpl[S, U]('path) }

  def modifiedFromPathImpl[T: Type, U: Type](path: Expr[T => U])(using Quotes): Expr[List[String]] = {
    import quotes.reflect.*

    enum PathElement {
      case TermPathElement(term: String, xargs: String*) extends PathElement
      case FunctorPathElement(functor: String, method: String, xargs: String*) extends PathElement
    }

    def toPath(tree: Tree, acc: List[PathElement]): Seq[PathElement] = {
      def typeSupported(modifyType: String) =
        Seq("DiffxEach", "DiffxEither", "DiffxEachMap")
          .exists(modifyType.endsWith)

      tree match {
        /** Field access */
        case Select(deep, ident) =>
          toPath(deep, PathElement.TermPathElement(ident) :: acc)
        /** Method call with no arguments and using clause */
        case Apply(Apply(TypeApply(Ident(f), _), idents), _) if typeSupported(f) => {
          val newAcc = acc match {
            /** replace the term controlled by quicklens */
            case PathElement.TermPathElement(term, xargs @ _*) :: rest => PathElement.FunctorPathElement(f, term, xargs: _*) :: rest
            case elements => report.throwError(s"Invalid use of path elements [${elements.mkString(", ")}]. $ShapeInfo, got: ${tree}")
          }

          idents.flatMap(toPath(_, newAcc))
        }

        /** The first segment from path (e.g. `_.age` -> `_`) */
        case i: Ident =>
          acc
        case t =>
          report.throwError(s"Unsupported path element $t")
      }
    }

    val pathElements = path.asTerm match {
      /** Single inlined path */
      case Inlined(_, _, Block(List(DefDef(_, _, _, Some(p))), _)) =>
        toPath(p, List.empty)
      case _ =>
        report.throwError(s"Unsupported path [$path]")
    }

    '{
    val pathValue = ${ Expr(pathElements.collect {
      case PathElement.TermPathElement(c) => c
      case PathElement.FunctorPathElement("DiffxEither", method, _ @_*) => method
    }.toList) }

    pathValue
    }
  }
}
