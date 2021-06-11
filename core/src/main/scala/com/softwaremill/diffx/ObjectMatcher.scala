package com.softwaremill.diffx

/*
  Used to pair elements within unordered containers like sets
 */
trait ObjectMatcher[T] {
  def isSameObject(left: T, right: T): Boolean
}

object ObjectMatcher {
  def apply[T: ObjectMatcher]: ObjectMatcher[T] = implicitly[ObjectMatcher[T]]

  implicit def default[T]: ObjectMatcher[T] = (l: T, r: T) => l == r

  def by[T, U: ObjectMatcher](f: T => U): ObjectMatcher[T] = (left: T, right: T) =>
    ObjectMatcher[U].isSameObject(f(left), f(right))
}
