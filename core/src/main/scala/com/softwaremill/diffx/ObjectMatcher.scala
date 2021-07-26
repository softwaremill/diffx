package com.softwaremill.diffx

/** Defines how the elements within collections are paired
  * @tparam T
  */
trait ObjectMatcher[T] {
  def isSameObject(left: T, right: T): Boolean
}

object ObjectMatcher extends LowPriorityObjectMatcher {
  def apply[T: ObjectMatcher]: ObjectMatcher[T] = implicitly[ObjectMatcher[T]]

  /** Given product of type T and its property U, match that products using U's objectMatcher */
  def by[T, U: ObjectMatcher](f: T => U): ObjectMatcher[T] = (left: T, right: T) =>
    ObjectMatcher[U].isSameObject(f(left), f(right))

  /** Given MapEntry[K,V] match them using V's objectMatcher */
  def byValue[K, V: ObjectMatcher]: ObjectMatcher[MapEntry[K, V]] = ObjectMatcher.by[MapEntry[K, V], V](_.value)

  /** Given MapEntry[K,V], where V is a type of product and U is a property of V, match them using U's objectMatcher */
  def byValue[K, V, U: ObjectMatcher](f: V => U): ObjectMatcher[MapEntry[K, V]] =
    ObjectMatcher.byValue[K, V](ObjectMatcher.by[V, U](f))

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
}

trait LowPriorityObjectMatcher {
  implicit def default[T]: ObjectMatcher[T] = (l: T, r: T) => l == r
}
