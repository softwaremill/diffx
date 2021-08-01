package com.softwaremill.diffx.instances.string

import com.softwaremill.diffx.instances.string.Delta.TYPE

private[instances] sealed abstract class Delta[T](original: Chunk[T], revised: Chunk[T]) {

  def getType: TYPE
  def getOriginal: Chunk[T] = original
  def getRevised: Chunk[T] = revised
  def getSource: Chunk[T] = original
  def getTarget: Chunk[T] = revised
  override def toString: String = s"Delta($getType, $getOriginal, $getRevised)"
}

private[instances] object Delta {
  sealed abstract class TYPE
  object TYPE {
    case object CHANGE extends TYPE
    case object DELETE extends TYPE
    case object INSERT extends TYPE
  }
}
private[instances] class ChangeDelta[T](original: Chunk[T], revised: Chunk[T]) extends Delta(original, revised) {
  override def getType: TYPE = TYPE.CHANGE
}
private[instances] class InsertDelta[T](original: Chunk[T], revised: Chunk[T]) extends Delta(original, revised) {
  override def getType: TYPE = TYPE.INSERT
}
private[instances] class DeleteDelta[T](original: Chunk[T], revised: Chunk[T]) extends Delta(original, revised) {
  override def getType: TYPE = TYPE.DELETE
}
