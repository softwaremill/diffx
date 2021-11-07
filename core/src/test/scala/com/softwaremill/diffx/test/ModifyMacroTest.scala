package com.softwaremill.diffx.test

import com.softwaremill.diffx._
import com.softwaremill.diffx.ModifyMacro
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.reflect.ClassTag

class ModifyMacroTest extends AnyFlatSpec with Matchers {
  it should "ignore field in nested products" in {
    ModifyMacro.modifiedFromPath[Family, String](_.first.name) shouldBe List(
      ModifyPath.Field("first"),
      ModifyPath.Field("name")
    )
  }

  it should "ignore fields in list of products" in {
    ModifyMacro.modifiedFromPath[Organization, String](_.people.each.name) shouldBe List(
      ModifyPath.Field("people"),
      ModifyPath.Each,
      ModifyPath.Field("name")
    )
  }

  it should "ignore fields in product wrapped with either" in {
    ModifyMacro.modifiedFromPath[Either[Person, Person], String](_.eachRight.name) shouldBe List(
      ModifyPath.Subtype("scala.package", "Right"),
      ModifyPath.Field("name")
    )
    ModifyMacro.modifiedFromPath[Either[Person, Person], String](_.eachLeft.name) shouldBe List(
      ModifyPath.Subtype("scala.package", "Left"),
      ModifyPath.Field("name")
    )
  }

  it should "ignore fields in product wrapped with option" in {
    ModifyMacro.modifiedFromPath[Option[Person], String](_.each.name) shouldBe List(
      ModifyPath.Each,
      ModifyPath.Field("name")
    )
  }

  it should "ignore part of map value" in {
    ModifyMacro.modifiedFromPath[Map[String, Person], String](_.eachValue.name) shouldBe List(
      ModifyPath.EachValue,
      ModifyPath.Field("name")
    )
  }

  it should "ignore part of map key" in {
    ModifyMacro.modifiedFromPath[Map[Person, String], String](_.eachKey.name) shouldBe List(
      ModifyPath.EachKey,
      ModifyPath.Field("name")
    )
  }

  it should "ignore part of set value" in {
    ModifyMacro.modifiedFromPath[Set[Person], String](_.each.name) shouldBe List(
      ModifyPath.Each,
      ModifyPath.Field("name")
    )
  }
}
