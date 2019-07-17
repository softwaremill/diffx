package com.softwaremill.diffx

trait EntityMatcher[T] {
  def isSameEntity(left: T, right: T): Boolean
}

object EntityMatcher {
  implicit def default[T]: EntityMatcher[T] = (l: T, r: T) => l == r
}
