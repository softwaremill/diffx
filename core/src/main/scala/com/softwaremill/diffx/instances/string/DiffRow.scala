package com.softwaremill.diffx.instances.string

private[instances] sealed trait DiffRow[T]
private[instances] object DiffRow {
  case class Insert[T](newLine: T) extends DiffRow[T]
  case class Delete[T](oldLine: T) extends DiffRow[T]
  case class Change[T](oldLine: T, newLine: T) extends DiffRow[T]
  case class Equal[T](oldLine: T, newLine: T) extends DiffRow[T]
}
