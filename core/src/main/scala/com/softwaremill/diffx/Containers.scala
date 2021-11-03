package com.softwaremill.diffx

trait SeqLike[C[_]] {
  def asSeq[A](c: C[A]): Seq[A]
}

object SeqLike {
  implicit def seqLike[C[W] <: scala.collection.Seq[W]]: SeqLike[C] = new SeqLike[C] {
    override def asSeq[A](c: C[A]): Seq[A] = c.toSeq
  }
}

trait SetLike[C[_]] {
  def asSet[A](c: C[A]): Set[A]
}

object SetLike {
  implicit def setLike[C[W] <: scala.collection.Set[W]]: SetLike[C] = new SetLike[C] {
    override def asSet[A](c: C[A]): Set[A] = c.toSet
  }
}

trait MapLike[C[_, _]] {
  def asMap[K, V](c: C[K, V]): Map[K, V]
}
object MapLike {
  implicit def mapLike[C[KK, VV] <: scala.collection.Map[KK, VV]]: MapLike[C] = new MapLike[C] {
    override def asMap[K, V](c: C[K, V]): Map[K, V] = c.toMap
  }
}
