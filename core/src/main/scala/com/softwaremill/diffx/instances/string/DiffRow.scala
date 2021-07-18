package com.softwaremill.diffx.instances.string

object DiffRow {
  type Tag = Tag.Value
  object Tag extends Enumeration {
    val INSERT, DELETE, CHANGE, EQUAL = Value
  }
}

case class DiffRow(tag: DiffRow.Tag, oldLine: String, newLine: String)
