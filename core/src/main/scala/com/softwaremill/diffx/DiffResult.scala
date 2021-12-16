package com.softwaremill.diffx

sealed trait DiffResult extends Product with Serializable {
  def isIdentical: Boolean

  def show()(implicit sc: ShowConfig): String =
    DiffResultPrinter.showIndented(sc.transformer(this), DiffResultPrinter.IndentLevel)
}

object DiffResult {
  val Ignored: IdenticalValue[Any] = IdenticalValue("<ignored>")
}

case class DiffResultObject(name: String, fields: Map[String, DiffResult]) extends DiffResult {
  override def isIdentical: Boolean = fields.values.forall(_.isIdentical)
}

case class DiffResultIterable(name: String, items: Map[String, DiffResult]) extends DiffResult {
  override def isIdentical: Boolean = items.values.forall(_.isIdentical)
}

case class DiffResultMap(entries: Map[DiffResult, DiffResult]) extends DiffResult {
  override def isIdentical: Boolean = entries.forall { case (k, v) => k.isIdentical && v.isIdentical }
}

case class DiffResultSet(diffs: Set[DiffResult]) extends DiffResult {
  override def isIdentical: Boolean = diffs.forall(_.isIdentical)
}

case class DiffResultString(diffs: List[DiffResult]) extends DiffResult {
  override def isIdentical: Boolean = diffs.forall(_.isIdentical)
}

case class DiffResultStringLine(diffs: List[DiffResult]) extends DiffResult {
  override def isIdentical: Boolean = diffs.forall(_.isIdentical)
}

case class DiffResultStringWord(diffs: List[DiffResult]) extends DiffResult {
  override def isIdentical: Boolean = diffs.forall(_.isIdentical)
}

case class DiffResultChunk(left: String, right: String) extends DiffResult {
  override def isIdentical: Boolean = false
}

case class DiffResultValue[T](left: T, right: T) extends DiffResult {
  override def isIdentical: Boolean = false
}

case class IdenticalValue[T](value: T) extends DiffResult {
  override def isIdentical: Boolean = true
}

case class DiffResultMissing[T](value: T) extends DiffResult {
  override def isIdentical: Boolean = false
}

case class DiffResultMissingChunk(value: String) extends DiffResult {
  override def isIdentical: Boolean = false
}

case class DiffResultAdditional[T](value: T) extends DiffResult {
  override def isIdentical: Boolean = false
}

case class DiffResultAdditionalChunk(value: String) extends DiffResult {
  override def isIdentical: Boolean = false
}
