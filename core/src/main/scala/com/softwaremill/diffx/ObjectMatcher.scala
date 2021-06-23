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

  /** Given key-value (K,V) pairs match them using V's objectMatcher */
  def byValue[K, V: ObjectMatcher]: ObjectMatcher[(K, V)] = ObjectMatcher.by[(K, V), V](_._2)

  /** Given key-value (K,V) pairs, where V is a type of product and U is a property of V, match them using U's objectMatcher */
  def byValue[K, V, U: ObjectMatcher](f: V => U): ObjectMatcher[(K, V)] =
    ObjectMatcher.byValue[K, V](ObjectMatcher.by[V, U](f))

  implicit def optionMatcher[T: ObjectMatcher]: ObjectMatcher[Option[T]] = (left: Option[T], right: Option[T]) => {
    (left, right) match {
      case (Some(l), Some(r)) => ObjectMatcher[T].isSameObject(l, r)
      case _                  => false
    }
  }

  /** Given key-value (K,V) pairs, match them using K's objectMatcher */
  implicit def byKey[K: ObjectMatcher, V]: ObjectMatcher[(K, V)] = ObjectMatcher.by[(K, V), K](_._1)
}

trait LowPriorityObjectMatcher {
  implicit def default[T]: ObjectMatcher[T] = (l: T, r: T) => l == r
}
