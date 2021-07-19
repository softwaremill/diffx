package com.softwaremill.diffx.instances.string

object DiffRow {

  sealed trait Tag
  object Tag {
    case object INSERT extends Tag
    case object DELETE extends Tag
    case object CHANGE extends Tag
    case object EQUAL extends Tag
  }
}

case class DiffRow(tag: DiffRow.Tag, oldLine: String, newLine: String)
