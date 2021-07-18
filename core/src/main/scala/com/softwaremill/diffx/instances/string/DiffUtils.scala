package com.softwaremill.diffx.instances.string

import java.util

object DiffUtils {
  def diff(
      original: util.List[String],
      revised: util.List[String]
  ): Patch[String] =
    new MyersDiff[String]().diff(original, revised)
}
