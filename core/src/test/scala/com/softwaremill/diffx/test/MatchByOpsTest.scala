package com.softwaremill.diffx.test

import com.softwaremill.diffx.generic.AutoDerivation
import com.softwaremill.diffx.test.ACoproduct.{ProductA, ProductB}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.softwaremill.diffx._

class MatchByOpsTest extends AnyFlatSpec with AutoDerivation with Matchers {
  it should "compile when using lens extensions to modify matcher" in {
    Diff.summon[Organization].modify(_.people).matchByValue(_.age)
  }

  it should "compile when using diff extensions to modify matcher" in {
    Diff.summon[List[Person]].matchByValue(_.age)
  }

  it should "allow to ignore property on a subtype" in {
    import com.softwaremill.diffx._
    val coproductDiff = Diff[ACoproduct].modify(_.subtype[ProductA].id).ignore

    coproductDiff(ProductA("ignored"), ProductA("ignored-again")).isIdentical shouldBe true
    coproductDiff(ProductB("ignored"), ProductB("ignored-again")).isIdentical shouldBe false
  }

  ignore should "fail to compile when using subtype selector alone" in {
    // this fails to compile on a clean build
    // assertDoesNotCompile("Diff[ACoproduct].modify(_.subtype[ProductA]).ignore")
  }
}
