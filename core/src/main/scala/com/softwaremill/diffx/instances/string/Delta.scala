package com.softwaremill.diffx.instances.string

private[instances] sealed trait Delta[T] {
  def original: Chunk[T]
  def revised: Chunk[T]
  def getSource: Chunk[T] = original
  def getTarget: Chunk[T] = revised
}

private[instances] case class ChangeDelta[T](original: Chunk[T], revised: Chunk[T]) extends Delta[T]
private[instances] case class InsertDelta[T](original: Chunk[T], revised: Chunk[T]) extends Delta[T]
private[instances] case class DeleteDelta[T](original: Chunk[T], revised: Chunk[T]) extends Delta[T]
