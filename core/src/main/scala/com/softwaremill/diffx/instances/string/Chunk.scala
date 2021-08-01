package com.softwaremill.diffx.instances.string

private[instances] case class Chunk[T](position: Int, lines: List[T]) {
  def size: Int = lines.size
  def last: Int = position + size - 1
}
