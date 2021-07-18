package com.softwaremill.diffx.instances.string

import java.util

class Chunk[T](position: Int, lines: util.List[T]) {

  def getPosition: Int = position
  def getLines: util.List[T] = lines
  def size: Int = lines.size()
  def last: Int = getPosition + size - 1
  override def toString = s"Chunk($getPosition, $getLines, $size)"
}
