package com.softwaremill.diffx

/** Defines how the elements within collections are paired
  * @tparam T
  */
trait ObjectMatcher[T] {
  def isSameObject(left: T, right: T): Boolean
}

object ObjectMatcher extends LowPriorityObjectMatcher {
  def apply[T: ObjectMatcher]: ObjectMatcher[T] = implicitly[ObjectMatcher[T]]

  private def by[T, U: ObjectMatcher](f: T => U): ObjectMatcher[T] = (left: T, right: T) =>
    ObjectMatcher[U].isSameObject(f(left), f(right))

  implicit def optionMatcher[T: ObjectMatcher]: ObjectMatcher[Option[T]] = (left: Option[T], right: Option[T]) => {
    (left, right) match {
      case (Some(l), Some(r)) => ObjectMatcher[T].isSameObject(l, r)
      case _                  => false
    }
  }

  /** Given MapEntry[K,V], match them using K's objectMatcher */
  implicit def byKey[K: ObjectMatcher, V]: ObjectMatcher[MapEntry[K, V]] = ObjectMatcher.by[MapEntry[K, V], K](_.key)

  type IterableEntry[T] = MapEntry[Int, T]
  case class MapEntry[K, V](key: K, value: V)

  def list[T] = new ObjectMatcherListHelper[T]

  class ObjectMatcherListHelper[V] {
    def byValue[U: ObjectMatcher](f: V => U): ObjectMatcher[IterableEntry[V]] = byValue(ObjectMatcher.by[V, U](f))
    def byValue(implicit ev: ObjectMatcher[V]): ObjectMatcher[IterableEntry[V]] =
      ObjectMatcher.by[IterableEntry[V], V](_.value)

    def byKey[U: ObjectMatcher](f: Int => U): ObjectMatcher[IterableEntry[V]] = byKey(ObjectMatcher.by(f))
    def byKey(implicit ko: ObjectMatcher[Int]): ObjectMatcher[IterableEntry[V]] = ObjectMatcher.by(_.key)
  }

  def set[T] = new ObjectMatcherSetHelper[T]

  class ObjectMatcherSetHelper[T] {
    def by[U: ObjectMatcher](f: T => U): ObjectMatcher[T] = (left: T, right: T) =>
      ObjectMatcher[U].isSameObject(f(left), f(right))
  }

  def map[K, V] = new ObjectMatcherMapHelper[K, V]

  class ObjectMatcherMapHelper[K, V] {
    def byValue[U: ObjectMatcher](f: V => U): ObjectMatcher[MapEntry[K, V]] = byValue(ObjectMatcher.by(f))
    def byValue(implicit ev: ObjectMatcher[V]): ObjectMatcher[MapEntry[K, V]] = ObjectMatcher.by(_.value)

    def byKey[U: ObjectMatcher](f: K => U): ObjectMatcher[MapEntry[K, V]] = byKey(ObjectMatcher.by(f))
    def byKey(implicit ko: ObjectMatcher[K]): ObjectMatcher[MapEntry[K, V]] = ObjectMatcher.by(_.key)
  }
}

trait LowPriorityObjectMatcher {
  implicit def default[T]: ObjectMatcher[T] = (l: T, r: T) => l == r
}
