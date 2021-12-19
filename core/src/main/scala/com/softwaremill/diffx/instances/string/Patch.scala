package com.softwaremill.diffx.instances.string

import java.util
import java.util.{Collections, Comparator}

private[instances] class Patch[T] {
  private val deltas: util.List[Delta[T]] = new util.ArrayList()
  private val comparator: Comparator[Delta[T]] = new Comparator[Delta[T]] {
    override def compare(o1: Delta[T], o2: Delta[T]): Int =
      o1.original.position.compareTo(o2.original.position)
  }
  def addDelta(delta: Delta[T]): Unit = {
    deltas.add(delta)
  }
  def getDeltas: util.List[Delta[T]] = {
    Collections.sort(deltas, comparator)
    deltas
  }

  override def toString: String = s"Patch($deltas)"
}
