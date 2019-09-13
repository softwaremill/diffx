package com.softwaremill.diffx

/*
  Used to pair elements within unordered containers like sets
 */
trait ObjectMatcher[T] {
  def isSameObject(left: T, right: T): Boolean
}

object ObjectMatcher {
  implicit def default[T]: ObjectMatcher[T] = (l: T, r: T) => l == r
}
