package com.softwaremill.diffx.test

import com.softwaremill.diffx.generic.AutoDerivation
import org.scalatest.flatspec.AnyFlatSpec

class MatchByOpsTest extends AnyFlatSpec with AutoDerivation {
  it should "compile when using lens extensions to modify matcher" in {
    assertCompiles("""
        |import com.softwaremill.diffx.Diff
        |
        |Diff.summon[Organization].modify(_.people).matchByValue(_.age)
        |""".stripMargin)
  }

  it should "compile when using diff extensions to modify matcher" in {
    assertCompiles("""
        |import com.softwaremill.diffx.Diff
        |
        |Diff.summon[List[Person]].matchByValue(_.age)
        |""".stripMargin)
  }
}
