package com.softwaremill.diffx

import scala.quoted.*

object ModifyMacro {
  private val ShapeInfo = "Path must have shape: _.field1.field2.each.field3.(...)"

  def ignoreMacro[T: Type, U: Type](
      base: Expr[Diff[T]]
  )(path: Expr[T => U], conf: Expr[DiffConfiguration])(using Quotes): Expr[Diff[T]] = {
    '{ ${ modifyMacro(base)(path) }.ignore($conf) }
  }

  def modifyMacro[T: Type, U: Type](base: Expr[Diff[T]])(path: Expr[T => U])(using Quotes): Expr[DiffLens[T, U]] = {
    '{
      val p = ${ modifiedFromPathImpl(path) }
      new com.softwaremill.diffx.DiffLens[T, U]($base, p)
    }
  }

  private[diffx] inline def modifiedFromPath[S, U](inline path: S => U): List[ModifyPath] = ${
    ModifyMacro.modifiedFromPathImpl[S, U]('path)
  }

  def modifiedFromPathImpl[T: Type, U: Type](path: Expr[T => U])(using Quotes): Expr[List[ModifyPath]] = {
    import quotes.reflect.*

    enum PathElement {
      case TermPathElement(term: String, xargs: String*) extends PathElement
      case FunctorPathElement(functor: String, method: String, xargs: String*) extends PathElement
      case SubtypePathElement(owner: String, short: String) extends PathElement
    }

    given ToExpr[ModifyPath] with {
      def apply(mp: ModifyPath)(using Quotes): Expr[ModifyPath] =
        mp match {
          case com.softwaremill.diffx.ModifyPath.Field(name) =>
            '{ com.softwaremill.diffx.ModifyPath.Field(${ Expr(name) }) }
          case _: com.softwaremill.diffx.ModifyPath.Each.type      => '{ com.softwaremill.diffx.ModifyPath.Each }
          case _: com.softwaremill.diffx.ModifyPath.EachKey.type   => '{ com.softwaremill.diffx.ModifyPath.EachKey }
          case _: com.softwaremill.diffx.ModifyPath.EachValue.type => '{ com.softwaremill.diffx.ModifyPath.EachValue }
          case com.softwaremill.diffx.ModifyPath.Subtype(owner, short) =>
            '{ com.softwaremill.diffx.ModifyPath.Subtype(${ Expr(owner) }, ${ Expr(short) }) }
        }
    }

    def resolveSubTypeName(typeTree: Tree)(using Quotes): String =
      typeTree match {
        case TypeIdent(name)     => name.toString
        case TypeSelect(_, name) => name.toString
      }

    def toPath(tree: Tree, acc: List[PathElement]): Seq[PathElement] = {
      def typeSupported(modifyType: String) =
        Seq("DiffxEach", "DiffxEither", "DiffxEachMap", "toSubtypeSelector")
          .exists(modifyType.endsWith)

      tree match {
        /** Field access */
        case Select(deep, ident) =>
          toPath(deep, PathElement.TermPathElement(ident) :: acc)
        /** Method call with no arguments and using clause */
        case Apply(Apply(TypeApply(Ident(f), _), idents), _) if typeSupported(f) =>
          val newAcc = acc match {
            /** replace the term controlled by quicklens */
            case PathElement.TermPathElement(term, xargs @ _*) :: rest =>
              PathElement.FunctorPathElement(f, term, xargs: _*) :: rest
            case elements =>
              report.throwError(s"Invalid use of path elements [${elements.mkString(", ")}]. $ShapeInfo, got: ${tree}")
          }
          idents.flatMap(toPath(_, newAcc))
        case x @ TypeApply(Select(Apply(TypeApply(Ident(f), superType :: Nil), rest :: Nil), _), subtype :: Nil)
            if typeSupported(f) =>
          if (superType.symbol.children.contains(subtype.symbol)) {
            toPath(
              rest,
              PathElement.SubtypePathElement(superType.symbol.fullName.toString, resolveSubTypeName(subtype)) :: acc
            )
          } else {
            report.throwError(
              s"subtype requires that the super type be a sealed trait (enum), and the subtype being a direct children of the super type.",
              x.asExpr
            )
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
      val pathValue = ${
        Expr(pathElements.collect {
          case PathElement.TermPathElement(c) =>
            com.softwaremill.diffx.ModifyPath.Field(c): ModifyPath
          case PathElement.FunctorPathElement("DiffxEither", "eachLeft", _ @_*) =>
            ModifyPath.Subtype("scala.package", "Left")
          case PathElement.FunctorPathElement("DiffxEither", "eachRight", _ @_*) =>
            ModifyPath.Subtype("scala.package", "Right")
          case PathElement.FunctorPathElement(_, "each", _ @_*)      => ModifyPath.Each
          case PathElement.FunctorPathElement(_, "eachKey", _ @_*)   => ModifyPath.EachKey
          case PathElement.FunctorPathElement(_, "eachValue", _ @_*) => ModifyPath.EachValue
          case PathElement.SubtypePathElement(owner, subtype)        => ModifyPath.Subtype(owner, subtype)
        }.toList)
      }

      pathValue
    }
  }
}
