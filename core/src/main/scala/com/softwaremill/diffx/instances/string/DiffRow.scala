package com.softwaremill.diffx.instances.string

private[instances] object DiffRow {

  sealed trait Tag
  object Tag {
    case object INSERT extends Tag
    case object DELETE extends Tag
    case object CHANGE extends Tag
    case object EQUAL extends Tag
  }
}

private[instances] case class DiffRow[T](tag: DiffRow.Tag, oldLine: Option[T], newLine: Option[T])
