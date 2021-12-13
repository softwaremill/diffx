package com.softwaremill.diffx.test

import com.softwaremill.diffx.{Diff, IdenticalValue}
import com.softwaremill.diffx.DiffxSupport.DiffxSubtypeSelector
import com.softwaremill.diffx.generic.AutoDerivation
import com.softwaremill.diffx.test.ACoproduct.{ProductA, ProductB}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MatchByOpsTest extends AnyFlatSpec with AutoDerivation with Matchers {
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

  it should "allow to ignore property on a subtype" in {
    val coproductDiff = Diff[ACoproduct].modify(_.subtype[ProductA].id).ignore

    coproductDiff(ProductA("ignored"), ProductA("ignored-again")).isIdentical shouldBe true
    coproductDiff(ProductB("ignored"), ProductB("ignored-again")).isIdentical shouldBe false
  }

  it should "fail to compile when using subtype selector alone" in {
    assertDoesNotCompile("Diff[ACoproduct].modify(_.subtype[ProductA]).ignore")
  }
}
