package com.softwaremill.diffx
import acyclic.skipped
import DiffResult._

trait DiffResult {
  def isIdentical: Boolean

  def show: String = showIndented(indentLevel)

  private[diffx] def showIndented(indent: Int): String
}

object DiffResult {
  private[diffx] final val indentLevel = 5
}

case class DiffResultObject(name: String, fields: Map[String, DiffResult]) extends DiffResultDifferent {
  override private[diffx] def showIndented(indent: Int): String = {
    val showFields = fields.map(f => s"${i(indent)}${f._1}: ${f._2.showIndented(indent + indentLevel)}")
    s"""$name(
       |${showFields.mkString(",\n")})""".stripMargin
  }
}

case class DiffResultMap(fields: Map[DiffResult, DiffResult]) extends DiffResultDifferent {
  override private[diffx] def showIndented(indent: Int): String = {
    val showFields =
      fields.map(f =>
        s"${i(indent)}${f._1.showIndented(indent + indentLevel)}: ${f._2.showIndented(indent + indentLevel)}"
      )
    s"""Map(
       |${showFields.mkString(",\n")})""".stripMargin
  }
}

case class DiffResultSet(diffs: List[DiffResult]) extends DiffResultDifferent {
  override private[diffx] def showIndented(indent: Int): String = {
    val showFields = diffs.map(f => s"${i(indent)}${f.showIndented(indent + indentLevel)}")
    showFields.mkString("Set(\n", ",\n", ")")
  }
}

trait DiffResultDifferent extends DiffResult {
  override def isIdentical: Boolean = false

  protected def i(indent: Int): String = " " * indent
}

case class DiffResultValue[T](left: T, right: T) extends DiffResultDifferent {
  override def showIndented(indent: Int): String = showChange(s"$left", s"$right")
}

case class Identical[T](value: T) extends DiffResult {
  override def isIdentical: Boolean = true

  override def showIndented(indent: Int): String = s"$value"
}

case class DiffResultMissing[T](value: T) extends DiffResultDifferent {
  override def showIndented(indent: Int): String = {
    red(s"$value")
  }
}

case class DiffResultAdditional[T](value: T) extends DiffResultDifferent {
  override def showIndented(indent: Int): String = {
    green(s"$value")
  }
}
