package com.softwaremill.diffx.instances.string

import scala.collection.JavaConverters._

private[instances] object DiffUtils {
  def diff[T](
      original: List[T],
      revised: List[T],
      equalizer: (T, T) => Boolean
  ): Patch[T] =
    new MyersDiff[T](equalizer).diff(original.asJava, revised.asJava)
}
