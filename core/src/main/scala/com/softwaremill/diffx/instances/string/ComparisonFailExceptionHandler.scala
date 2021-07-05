package com.softwaremill.diffx.instances.string

trait ComparisonFailExceptionHandler {
  def handle(
      message: String,
      obtained: String,
      expected: String
  ): Nothing
}
