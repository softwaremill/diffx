package com.softwaremill.diffx

case class DiffConfiguration(makeIgnored: Diff[Any] => Diff[Any])

object DiffConfiguration {
  implicit val Default: DiffConfiguration = DiffConfiguration(makeIgnored = (_: Diff[Any]) => Diff.ignored[Any])
}
