package com.softwaremill.diffx.test

import com.softwaremill.diffx._
import com.softwaremill.diffx.generic.AutoDerivation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant
import java.util.UUID

class DiffModifyIntegrationTest extends AnyFlatSpec with Matchers with AutoDerivation {
  val instant: Instant = Instant.now()
  val p1 = Person("p1", 22, instant)
  val p2 = Person("p2", 11, instant)

  it should "allow importing and exporting implicits" in {
    implicit val d: Diff[Person] = Diff.autoDerived[Person].ignore(_.name)
    compare(p1, p2) shouldBe DiffResultObject(
      "Person",
      Map("name" -> IdenticalValue("<ignored>"), "age" -> DiffResultValue(22, 11), "in" -> IdenticalValue(instant))
    )
  }

  it should "allow importing and exporting implicits using macro on derived instance" in {
    implicit val d: Diff[Person] = Diff.autoDerived[Person].ignore(_.name)
    compare(p1, p2) shouldBe DiffResultObject(
      "Person",
      Map("name" -> IdenticalValue("<ignored>"), "age" -> DiffResultValue(22, 11), "in" -> IdenticalValue(instant))
    )
  }

  it should "allow calling ignore multiple times" in {
    implicit val d: Diff[Person] = Diff
      .autoDerived[Person]
      .ignore(_.name)
      .ignore(_.age)
    compare(p1, p2).isIdentical shouldBe true
  }

  it should "compare lists using explicit object matcher comparator" in {
    val o1 = Organization(List(p1, p2))
    val o2 = Organization(List(p2, p1))
    implicit val orgDiff: Diff[Organization] = Diff
      .autoDerived[Organization]
      .modify(_.people)
      .matchByValue(_.name)
    compare(o1, o2).isIdentical shouldBe true
  }

  it should "ignore only on right" in {
    case class Wrapper(e: Either[Person, Person])
    val e1 = Wrapper(Right(p1))
    val e2 = Wrapper(Right(p1.copy(name = p1.name + "_modified")))

    implicit val wrapperDiff: Diff[Wrapper] = Diff.autoDerived[Wrapper].ignore(_.e.eachRight.name)

    compare(e1, e2).isIdentical shouldBe true

    val e3 = Wrapper(Left(p1))
    val e4 = Wrapper(Left(p1.copy(name = p1.name + "_modified")))

    compare(e3, e4).isIdentical shouldBe false
  }

  it should "ignore only on left" in {
    case class Wrapper(e: Either[Person, Person])
    val e1 = Wrapper(Right(p1))
    val e2 = Wrapper(Right(p1.copy(name = p1.name + "_modified")))

    implicit val wrapperDiff: Diff[Wrapper] = Diff.autoDerived[Wrapper].ignore(_.e.eachLeft.name)

    compare(e1, e2).isIdentical shouldBe false
    val e3 = Wrapper(Left(p1))
    val e4 = Wrapper(Left(p1.copy(name = p1.name + "_modified")))

    compare(e3, e4).isIdentical shouldBe true
  }

  it should "match map entries by values" in {
    implicit val lookupDiff: Diff[MyLookup] = Diff
      .autoDerived[MyLookup]
      .modify(_.map)
      .matchByValue(identity)
    val uuid1 = UUID.randomUUID()
    val uuid2 = UUID.randomUUID()
    val a1 = MyLookup(Map(KeyModel(uuid1, "k1") -> "val1"))
    val a2 = MyLookup(Map(KeyModel(uuid2, "k1") -> "val1"))
    compare(a1, a2) shouldBe DiffResultObject(
      "MyLookup",
      Map(
        "map" -> DiffResultMap(
          "Map",
          Map(
            DiffResultObject(
              "KeyModel",
              Map(
                "id" -> DiffResultValue(uuid1, uuid2),
                "name" -> IdenticalValue("k1")
              )
            ) -> IdenticalValue("val1")
          )
        )
      )
    )
  }

  it should "ignore part of each key in map" in {
    implicit val lookupDiff: Diff[MyLookup] = Diff
      .autoDerived[MyLookup]
      .ignore(_.map.eachKey.id)
      .modify(_.map)
      .matchByKey(_.name)
    val uuid1 = UUID.randomUUID()
    val uuid2 = UUID.randomUUID()
    val a1 = MyLookup(Map(KeyModel(uuid1, "k1") -> "val1"))
    val a2 = MyLookup(Map(KeyModel(uuid2, "k1") -> "val1"))
    compare(a1, a2) shouldBe DiffResultObject(
      "MyLookup",
      Map(
        "map" -> DiffResultMap(
          "Map",
          Map(
            DiffResultObject(
              "KeyModel",
              Map(
                "id" -> DiffResult.Ignored,
                "name" -> IdenticalValue("k1")
              )
            ) -> IdenticalValue("val1")
          )
        )
      )
    )
  }

  it should "ignore part of each value in map" in {
    implicit val lookupDiff: Diff[MyLookupReversed] = Diff
      .autoDerived[MyLookupReversed]
      .ignore(_.map.eachValue.id)
    val uuid1 = UUID.randomUUID()
    val uuid2 = UUID.randomUUID()
    val a1 = MyLookupReversed(Map("val1" -> KeyModel(uuid1, "k1")))
    val a2 = MyLookupReversed(Map("val1" -> KeyModel(uuid2, "k1")))
    compare(a1, a2) shouldBe DiffResultObject(
      "MyLookupReversed",
      Map(
        "map" -> DiffResultMap(
          "Map",
          Map(
            IdenticalValue("val1") -> DiffResultObject(
              "KeyModel",
              Map(
                "id" -> DiffResult.Ignored,
                "name" -> IdenticalValue("k1")
              )
            )
          )
        )
      )
    )
  }

  it should "ignore part of each value in a set" in {
    implicit val lookupDiff: Diff[Startup] = Diff
      .autoDerived[Startup]
      .ignore(_.workers.each.age)
      .modify(_.workers)
      .matchBy(_.name)
    val p2m = p2.copy(age = 33)
    compare(Startup(Set(p1, p2)), Startup(Set(p1, p2m))) shouldBe DiffResultObject(
      "Startup",
      Map(
        "workers" -> DiffResultSet(
          "Set",
          Set(
            DiffResultObject(
              "Person",
              Map(
                "name" -> IdenticalValue(p1.name),
                "age" -> DiffResult.Ignored,
                "in" -> IdenticalValue(p1.in)
              )
            ),
            DiffResultObject(
              "Person",
              Map(
                "name" -> IdenticalValue(p2.name),
                "age" -> DiffResult.Ignored,
                "in" -> IdenticalValue(p1.in)
              )
            )
          )
        )
      )
    )
  }

  it should "use overrided object matcher when comparing set" in {
    implicit val lookupDiff: Diff[Startup] = Diff
      .autoDerived[Startup]
      .modify(_.workers)
      .matchBy(_.name)
    val p2m = p2.copy(age = 33)
    compare(Startup(Set(p1, p2)), Startup(Set(p1, p2m))) shouldBe DiffResultObject(
      "Startup",
      Map(
        "workers" -> DiffResultSet(
          "Set",
          Set(
            DiffResultObject(
              "Person",
              Map(
                "name" -> IdenticalValue(p1.name),
                "age" -> IdenticalValue(p1.age),
                "in" -> IdenticalValue(p1.in)
              )
            ),
            DiffResultObject(
              "Person",
              Map(
                "name" -> IdenticalValue(p2.name),
                "age" -> DiffResultValue(p2.age, p2m.age),
                "in" -> IdenticalValue(p1.in)
              )
            )
          )
        )
      )
    )
  }

  it should "compare lists using object matcher comparator passed explicitly" in {
    val o1 = Organization(List(p1, p2))
    val o2 = Organization(List(p2, p1))
    val d = Diff[Organization]
      .modify(_.people)
      .matchByValue(_.name)
    compare(o1, o2)(d).isIdentical shouldBe true
  }

  it should "allow overriding how ignored diffs are produced" in {
    implicit val conf: DiffConfiguration = DiffConfiguration(makeIgnored =
      (original: Diff[Any]) =>
        (left: Any, right: Any, context: DiffContext) => {
          IdenticalValue(
            s"Ignored but was: ${original.apply(left, right, context).show()(ShowConfig.noColors)}"
          )
        }
    )
    implicit val d: Diff[Person] = Diff.autoDerived[Person].ignore(_.name)
    compare(p1, p2) shouldBe DiffResultObject(
      "Person",
      Map(
        "name" -> IdenticalValue("Ignored but was: p[1 -> 2]"),
        "age" -> DiffResultValue(22, 11),
        "in" -> IdenticalValue(instant)
      )
    )
  }

  it should "allow overriding how ignored diffs are produced - regular instance" in {
    implicit val conf: DiffConfiguration = DiffConfiguration(makeIgnored =
      (original: Diff[Any]) =>
        (left: Any, right: Any, context: DiffContext) => {
          IdenticalValue(
            s"Ignored but was: ${original.apply(left, right, context).show()(ShowConfig.noColors)}"
          )
        }
    )
    val d: Diff[Person] = Diff[Person].ignore(_.name)
    compare(p1, p2)(d) shouldBe DiffResultObject(
      "Person",
      Map(
        "name" -> IdenticalValue("Ignored but was: p[1 -> 2]"),
        "age" -> DiffResultValue(22, 11),
        "in" -> IdenticalValue(instant)
      )
    )
  }
}
