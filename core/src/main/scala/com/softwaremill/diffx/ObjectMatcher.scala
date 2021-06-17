package com.softwaremill.diffx

/*
  Used to pair elements within unordered containers like sets
 */
trait ObjectMatcher[T] {
  def isSameObject(left: T, right: T): Boolean
}

object ObjectMatcher extends LowPriorityObjectMatcher {
  def apply[T: ObjectMatcher]: ObjectMatcher[T] = implicitly[ObjectMatcher[T]]

  def by[T, U: ObjectMatcher](f: T => U): ObjectMatcher[T] = (left: T, right: T) =>
    ObjectMatcher[U].isSameObject(f(left), f(right))

  def byValue[K, V: ObjectMatcher]: ObjectMatcher[(K, V)] = ObjectMatcher.by[(K, V), V](_._2)

  implicit def optionMatcher[T: ObjectMatcher]: ObjectMatcher[Option[T]] = (left: Option[T], right: Option[T]) => {
    (left, right) match {
      case (Some(l), Some(r)) => ObjectMatcher[T].isSameObject(l, r)
      case _                  => false
    }
  }
  implicit def byKey[K: ObjectMatcher, V]: ObjectMatcher[(K, V)] = ObjectMatcher.by[(K, V), K](_._1)
}

trait LowPriorityObjectMatcher {
  implicit def default[T]: ObjectMatcher[T] = (l: T, r: T) => l == r

}
