package com.softwaremill.diffx.ziotest

import com.softwaremill.diffx.{Diff, DiffResultDifferent}
import zio.test.Assertion
import zio.test.Assertion.Render.param

trait ZIODiffMatcher {

  /**
    * Makes a new assertion that requires a value equal the specified value.
    * (Same as zio.test.Assertion.equalTo)
    *
    * Before this it calls  Diff[A].apply and prints the Result; if it fails.
    */
  final def matchTo[A: Diff](expected: A): Assertion[A] =
    Assertion.assertion("matchTo")(param(expected)) { actual =>
      // Run diffx and prints the Result; if it fails.
      Diff[A].apply(actual, expected) match {
        case c: DiffResultDifferent =>
          println(c.show)
        case _ => // nothing to do if success
      }
      // zio.test.Assertion.equalTo
      (actual, expected) match {
        case (left: Array[_], right: Array[_]) => left.sameElements[Any](right)
        case (left, right)                     => left == right
      }
    }

}

object ZIODiffMatcher extends ZIODiffMatcher
