package com.softwaremill.diffx
import acyclic.skipped
import DiffResult._

trait DiffResult extends Product with Serializable {
  def isIdentical: Boolean

  def show(implicit c: ConsoleColorConfig): String = showIndented(indentLevel)

  private[diffx] def showIndented(indent: Int)(implicit c: ConsoleColorConfig): String
}

object DiffResult {
  private[diffx] final val indentLevel = 5
}

case class DiffResultObject(name: String, fields: Map[String, DiffResult]) extends DiffResultDifferent {
  override private[diffx] def showIndented(indent: Int)(implicit c: ConsoleColorConfig): String = {
    val showFields =
      fields.map(f => s"${i(indent)}${defaultColor(s"${f._1}:")} ${f._2.showIndented(indent + indentLevel)}")
    defaultColor(s"$name(") + s"\n${showFields.mkString(defaultColor(",") + "\n")}" + defaultColor(")")
  }
}

case class DiffResultMap(fields: Map[DiffResult, DiffResult]) extends DiffResultDifferent {
  override private[diffx] def showIndented(indent: Int)(implicit c: ConsoleColorConfig): String = {
    val showFields =
      fields.map(f =>
        s"${i(indent)}${defaultColor(s"${f._1.showIndented(indent + indentLevel)}")}" + defaultColor(": ") + s"${f._2
          .showIndented(indent + indentLevel)}"
      )
    defaultColor("Map(") + s"\n${showFields.mkString(defaultColor(",") + "\n")}" + defaultColor(")")
  }
}

case class DiffResultSet(diffs: List[DiffResult]) extends DiffResultDifferent {
  override private[diffx] def showIndented(indent: Int)(implicit c: ConsoleColorConfig): String = {
    val showFields = diffs.map(f => s"${i(indent)}${f.showIndented(indent + indentLevel)}")
    showFields.mkString(defaultColor("Set(\n"), ",\n", defaultColor(")"))
  }
}

case class DiffResultString(diffs: List[DiffResult]) extends DiffResultDifferent {
  override private[diffx] def showIndented(indent: Int)(implicit c: ConsoleColorConfig): String = {
    s"${diffs.map(_.showIndented(indent)).mkString("\n")}"
  }
}

trait DiffResultDifferent extends DiffResult {
  override def isIdentical: Boolean = false

  protected def i(indent: Int): String = " " * indent
}

case class DiffResultValue[T](left: T, right: T) extends DiffResultDifferent {
  override def showIndented(indent: Int)(implicit c: ConsoleColorConfig): String = showChange(s"$left", s"$right")
}

case class Identical[T](value: T) extends DiffResult {
  override def isIdentical: Boolean = true

  override def showIndented(indent: Int)(implicit c: ConsoleColorConfig): String = defaultColor(s"$value")
}

case class DiffResultMissing[T](value: T) extends DiffResultDifferent {
  override def showIndented(indent: Int)(implicit c: ConsoleColorConfig): String = {
    rightColor(s"$value")
  }
}

case class DiffResultAdditional[T](value: T) extends DiffResultDifferent {
  override def showIndented(indent: Int)(implicit c: ConsoleColorConfig): String = {
    leftColor(s"$value")
  }
}
