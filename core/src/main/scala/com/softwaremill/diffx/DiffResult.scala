package com.softwaremill.diffx

import DiffResult._

trait DiffResult extends Product with Serializable {
  def isIdentical: Boolean

  def show()(implicit c: ShowConfig): String =
    showIndented(indentLevel)

  private[diffx] def showIndented(indent: Int)(implicit c: ShowConfig): String

  protected def i(indent: Int): String = " " * indent
}

object DiffResult {
  private[diffx] final val indentLevel = 5
  private[diffx] def mergeChunks(diffs: List[DiffResult]) = {
    diffs
      .foldLeft(List.empty[DiffResult]) { (acc, item) =>
        (acc.lastOption, item) match {
          case (Some(d: DiffResultMissingChunk), di: DiffResultMissingChunk) =>
            acc.dropRight(1) :+ d.copy(value = d.value + di.value)
          case (Some(d: DiffResultAdditionalChunk), di: DiffResultAdditionalChunk) =>
            acc.dropRight(1) :+ d.copy(value = d.value + di.value)
          case (Some(d: DiffResultChunk), di: DiffResultChunk) =>
            acc.dropRight(1) :+ d.copy(left = d.left + di.left, right = d.right + di.right)
          case _ => acc :+ item
        }
      }
  }

  val Ignored: IdenticalValue[Any] = IdenticalValue("<ignored>")

  private[diffx] def leftColor(s: String)(implicit c: ShowConfig): String = c.left(s)
  private[diffx] def rightColor(s: String)(implicit c: ShowConfig): String = c.right(s)
  private[diffx] def defaultColor(s: String)(implicit c: ShowConfig): String = c.default(s)
  private[diffx] def arrowColor(s: String)(implicit c: ShowConfig): String = c.arrow(s)
  private[diffx] def showChange(l: String, r: String)(implicit c: ShowConfig): String =
    leftColor(l) + arrowColor(" -> ") + rightColor(r)
}

case class DiffResultObject(name: String, fields: Map[String, DiffResult]) extends DiffResult {
  override private[diffx] def showIndented(indent: Int)(implicit
      c: ShowConfig
  ): String = {
    val showFields = fields
      .filter { case (_, v) =>
        c.renderIdentical || !v.isIdentical
      }
      .map { case (field, value) =>
        renderField(indent, field) + renderValue(indent, value)
      }
    defaultColor(s"$name(") + s"\n${showFields.mkString(defaultColor(",") + "\n")}" + defaultColor(")")
  }

  private def renderValue(indent: Int, value: DiffResult)(implicit
      c: ShowConfig
  ) = {
    s"${value.showIndented(indent + indentLevel)}"
  }

  private def renderField(indent: Int, field: String)(implicit
      c: ShowConfig
  ) = {
    s"${i(indent)}${defaultColor(s"$field: ")}"
  }

  override def isIdentical: Boolean = fields.values.forall(_.isIdentical)
}

case class DiffResultMap(entries: Map[DiffResult, DiffResult]) extends DiffResult {
  override private[diffx] def showIndented(indent: Int)(implicit
      c: ShowConfig
  ): String = {
    val showFields = entries
      .filter { case (k, v) =>
        c.renderIdentical || !v.isIdentical || !k.isIdentical
      }
      .map { case (k, v) =>
        val key = renderKey(indent, k)
        val separator = defaultColor(": ")
        val value = renderValue(indent, v)
        key + separator + value
      }
    defaultColor("Map(") + s"\n${showFields.mkString(defaultColor(",") + "\n")}" + defaultColor(")")
  }

  private def renderValue(indent: Int, value: DiffResult)(implicit
      c: ShowConfig
  ) = {
    value.showIndented(indent + indentLevel)
  }

  private def renderKey(indent: Int, key: DiffResult)(implicit
      c: ShowConfig
  ) = {
    s"${i(indent)}${defaultColor(s"${key.showIndented(indent + indentLevel)}")}"
  }

  override def isIdentical: Boolean = entries.forall { case (k, v) => k.isIdentical && v.isIdentical }
}

case class DiffResultSet(diffs: Set[DiffResult]) extends DiffResult {
  override private[diffx] def showIndented(indent: Int)(implicit
      c: ShowConfig
  ): String = {
    val showFields = diffs
      .filter(df => c.renderIdentical || !df.isIdentical)
      .map(f => s"${i(indent)}${f.showIndented(indent + indentLevel)}")
    showFields.mkString(defaultColor("Set(\n"), ",\n", defaultColor(")"))
  }

  override def isIdentical: Boolean = diffs.forall(_.isIdentical)
}

case class DiffResultString(diffs: List[DiffResult]) extends DiffResult {
  override private[diffx] def showIndented(indent: Int)(implicit
      c: ShowConfig
  ): String = {
    s"${diffs.map(_.showIndented(indent)).mkString("\n")}"
  }

  override def isIdentical: Boolean = diffs.forall(_.isIdentical)
}

case class DiffResultStringLine(diffs: List[DiffResult]) extends DiffResult {
  override private[diffx] def showIndented(indent: Int)(implicit
      c: ShowConfig
  ): String = {
    mergeChunks(diffs)
      .map(_.showIndented(indent))
      .mkString
  }

  override def isIdentical: Boolean = diffs.forall(_.isIdentical)
}

case class DiffResultStringWord(diffs: List[DiffResult]) extends DiffResult {
  override private[diffx] def showIndented(indent: Int)(implicit
      c: ShowConfig
  ): String = {
    mergeChunks(diffs)
      .map(_.showIndented(indent))
      .mkString
  }

  override def isIdentical: Boolean = diffs.forall(_.isIdentical)
}

case class DiffResultChunk(left: String, right: String) extends DiffResult {
  override def isIdentical: Boolean = false

  override private[diffx] def showIndented(indent: Int)(implicit c: ShowConfig) = {
    arrowColor("[") + showChange(s"$left", s"$right") + arrowColor("]")
  }
}

case class DiffResultValue[T](left: T, right: T) extends DiffResult {
  override def showIndented(indent: Int)(implicit c: ShowConfig): String =
    showChange(s"$left", s"$right")

  override def isIdentical: Boolean = false
}

case class IdenticalValue[T](value: T) extends DiffResult {
  override def isIdentical: Boolean = true

  override def showIndented(indent: Int)(implicit c: ShowConfig): String =
    defaultColor(s"$value")
}

case class DiffResultMissing[T](value: T) extends DiffResult {
  override def showIndented(indent: Int)(implicit c: ShowConfig): String = {
    leftColor(s"$value")
  }
  override def isIdentical: Boolean = false
}

case class DiffResultMissingChunk(value: String) extends DiffResult {
  override def showIndented(indent: Int)(implicit c: ShowConfig): String = {
    leftColor(s"[$value]")
  }
  override def isIdentical: Boolean = false
}

case class DiffResultAdditional[T](value: T) extends DiffResult {
  override def showIndented(indent: Int)(implicit c: ShowConfig): String = {
    rightColor(s"$value")
  }
  override def isIdentical: Boolean = false
}

case class DiffResultAdditionalChunk(value: String) extends DiffResult {
  override def showIndented(indent: Int)(implicit c: ShowConfig): String = {
    rightColor(s"[$value]")
  }
  override def isIdentical: Boolean = false
}
