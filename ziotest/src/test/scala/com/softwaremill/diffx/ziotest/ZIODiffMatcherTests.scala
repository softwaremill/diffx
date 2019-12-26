package com.softwaremill.diffx.ziotest

import zio.ZIO
import zio.test._
import TestAspect.ignore

object ZIODiffMatcherTests extends ZIODiffMatcher {
  val suites =
    suite("Run Program")(
      testM("the Configs are correct") {
        assertM(ZIO.effectTotal(left), matchTo(right))
      }
    ) @@ ignore // comment @@ ignore if you want the test to run
}

object ZIODiffMatcherSuites
  extends DefaultRunnableSpec(
    suite("ZIODiffMatcherSuites")(
      ZIODiffMatcherTests.suites
    ))