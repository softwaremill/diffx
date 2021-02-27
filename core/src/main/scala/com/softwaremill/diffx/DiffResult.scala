package com.softwaremill.diffx

import DiffResult._

trait DiffResult extends Product with Serializable {
  def isIdentical: Boolean

  def show(renderIdentical: Boolean = true)(implicit c: ConsoleColorConfig): String =
    showIndented(indentLevel, renderIdentical)

  private[diffx] def showIndented(indent: Int, renderIdentical: Boolean)(implicit c: ConsoleColorConfig): String
}

object DiffResult {
  private[diffx] final val indentLevel = 5
}

case class DiffResultObject(name: String, fields: Map[String, DiffResult]) extends DiffResultDifferent {
  override private[diffx] def showIndented(indent: Int, renderIdentical: Boolean)(implicit
      c: ConsoleColorConfig
  ): String = {
    val showFields = fields
      .filter { case (_, v) =>
        renderIdentical || !v.isIdentical
      }
      .map { case (field, value) =>
        renderField(indent, field) + renderValue(indent, renderIdentical, value)
      }
    defaultColor(s"$name(") + s"\n${showFields.mkString(defaultColor(",") + "\n")}" + defaultColor(")")
  }

  private def renderValue(indent: Int, renderIdentical: Boolean, value: DiffResult)(implicit
      c: ConsoleColorConfig
  ) = {
    s"${value.showIndented(indent + indentLevel, renderIdentical)}"
  }

  private def renderField(indent: Int, field: String)(implicit
      c: ConsoleColorConfig
  ) = {
    s"${i(indent)}${defaultColor(s"$field: ")}"
  }
}

case class DiffResultMap(fields: Map[DiffResult, DiffResult]) extends DiffResultDifferent {
  override private[diffx] def showIndented(indent: Int, renderIdentical: Boolean)(implicit
      c: ConsoleColorConfig
  ): String = {
    val showFields = fields
      .filter { case (k, v) =>
        renderIdentical || !v.isIdentical || !k.isIdentical
      }
      .map { case (k, v) =>
        val key = renderKey(indent, renderIdentical, k)
        val separator = defaultColor(": ")
        val value = renderValue(indent, renderIdentical, v)
        key + separator + value
      }
    defaultColor("Map(") + s"\n${showFields.mkString(defaultColor(",") + "\n")}" + defaultColor(")")
  }

  private def renderValue(indent: Int, renderIdentical: Boolean, value: DiffResult)(implicit
      c: ConsoleColorConfig
  ) = {
    value.showIndented(indent + indentLevel, renderIdentical)
  }

  private def renderKey(indent: Int, renderIdentical: Boolean, key: DiffResult)(implicit
      c: ConsoleColorConfig
  ) = {
    s"${i(indent)}${defaultColor(s"${key.showIndented(indent + indentLevel, renderIdentical)}")}"
  }
}

case class DiffResultSet(diffs: List[DiffResult]) extends DiffResultDifferent {
  override private[diffx] def showIndented(indent: Int, renderIdentical: Boolean)(implicit
      c: ConsoleColorConfig
  ): String = {
    val showFields = diffs
      .filter(df => renderIdentical || !df.isIdentical)
      .map(f => s"${i(indent)}${f.showIndented(indent + indentLevel, renderIdentical)}")
    showFields.mkString(defaultColor("Set(\n"), ",\n", defaultColor(")"))
  }
}

case class DiffResultString(diffs: List[DiffResult]) extends DiffResultDifferent {
  override private[diffx] def showIndented(indent: Int, renderIdentical: Boolean)(implicit
      c: ConsoleColorConfig
  ): String = {
    s"${diffs.map(_.showIndented(indent, renderIdentical)).mkString("\n")}"
  }
}

trait DiffResultDifferent extends DiffResult {
  override def isIdentical: Boolean = false

  protected def i(indent: Int): String = " " * indent
}

case class DiffResultValue[T](left: T, right: T) extends DiffResultDifferent {
  override def showIndented(indent: Int, renderIdentical: Boolean)(implicit c: ConsoleColorConfig): String =
    showChange(s"$left", s"$right")
}

case class Identical[T](value: T) extends DiffResult {
  override def isIdentical: Boolean = true

  override def showIndented(indent: Int, renderIdentical: Boolean)(implicit c: ConsoleColorConfig): String =
    defaultColor(s"$value")
}

case class DiffResultMissing[T](value: T) extends DiffResultDifferent {
  override def showIndented(indent: Int, renderIdentical: Boolean)(implicit c: ConsoleColorConfig): String = {
    rightColor(s"$value")
  }
}

case class DiffResultAdditional[T](value: T) extends DiffResultDifferent {
  override def showIndented(indent: Int, renderIdentical: Boolean)(implicit c: ConsoleColorConfig): String = {
    leftColor(s"$value")
  }
}
