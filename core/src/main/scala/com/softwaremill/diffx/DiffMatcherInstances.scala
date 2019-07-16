package com.softwaremill.diffx

trait DiffMatcherInstances {

  implicit def diffMatcherForAny[T]: DiffMatcher[T] = DiffMatcher.default
}

trait DiffMatcher[T] {
  def isSameInstance(left: T, right: T): Boolean
}

object DiffMatcher {
  def default[T]: DiffMatcher[T] = (_: T, _: T) => false
}
