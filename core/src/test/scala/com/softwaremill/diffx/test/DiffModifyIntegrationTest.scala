package com.softwaremill.diffx.test

import com.softwaremill.diffx._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.softwaremill.diffx.generic.auto._

import java.time.Instant
import java.util.UUID

class DiffModifyIntegrationTest extends AnyFlatSpec with Matchers {
  val instant: Instant = Instant.now()
  val p1 = Person("p1", 22, instant)
  val p2 = Person("p2", 11, instant)

  it should "allow importing and exporting implicits" in {
    implicit val d: Diff[Person] = Derived[Diff[Person]].ignore(_.name)
    compare(p1, p2) shouldBe DiffResultObject(
      "Person",
      Map("name" -> IdenticalValue("<ignored>"), "age" -> DiffResultValue(22, 11), "in" -> IdenticalValue(instant))
    )
  }

  it should "allow importing and exporting implicits using macro on derived instance" in {
    implicit val d: Diff[Person] = Derived[Diff[Person]].ignore(_.name)
    compare(p1, p2) shouldBe DiffResultObject(
      "Person",
      Map("name" -> IdenticalValue("<ignored>"), "age" -> DiffResultValue(22, 11), "in" -> IdenticalValue(instant))
    )
  }

  it should "allow calling ignore multiple times" in {
    implicit val d: Diff[Person] = Derived[Diff[Person]]
      .ignore(_.name)
      .ignore(_.age)
    compare(p1, p2) shouldBe IdenticalValue(p1)
  }

  it should "compare lists using explicit object matcher comparator" in {
    val o1 = Organization(List(p1, p2))
    val o2 = Organization(List(p2, p1))
    implicit val orgDiff: Diff[Organization] = Derived[Diff[Organization]]
      .modify(_.people)
      .withListMatcher(
        ObjectMatcher.byValue[Int, Person](ObjectMatcher.by(_.name))
      )
    compare(o1, o2) shouldBe IdenticalValue(Organization(List(p1, p2)))
  }

  it should "ignore only on right" in {
    case class Wrapper(e: Either[Person, Person])
    val e1 = Wrapper(Right(p1))
    val e2 = Wrapper(Right(p1.copy(name = p1.name + "_modified")))

    implicit val wrapperDiff: Diff[Wrapper] = Derived[Diff[Wrapper]].ignore(_.e.eachRight.name)

    compare(e1, e2) shouldBe IdenticalValue(e1)

    val e3 = Wrapper(Left(p1))
    val e4 = Wrapper(Left(p1.copy(name = p1.name + "_modified")))

    compare(e3, e4) should not be an[IdenticalValue[_]]
  }

  it should "ignore only on left" in {
    case class Wrapper(e: Either[Person, Person])
    val e1 = Wrapper(Right(p1))
    val e2 = Wrapper(Right(p1.copy(name = p1.name + "_modified")))

    implicit val wrapperDiff: Diff[Wrapper] = Derived[Diff[Wrapper]].ignore(_.e.eachLeft.name)

    compare(e1, e2) should not be an[IdenticalValue[_]]
    val e3 = Wrapper(Left(p1))
    val e4 = Wrapper(Left(p1.copy(name = p1.name + "_modified")))

    compare(e3, e4) shouldBe an[IdenticalValue[_]]
  }

  it should "match map entries by values" in {
    implicit val lookupDiff: Diff[MyLookup] = Derived[Diff[MyLookup]]
      .modify(_.map)
      .withMapMatcher(
        ObjectMatcher.byValue[KeyModel, String]
      )
    val uuid1 = UUID.randomUUID()
    val uuid2 = UUID.randomUUID()
    val a1 = MyLookup(Map(KeyModel(uuid1, "k1") -> "val1"))
    val a2 = MyLookup(Map(KeyModel(uuid2, "k1") -> "val1"))
    compare(a1, a2) shouldBe DiffResultObject(
      "MyLookup",
      Map(
        "map" -> DiffResultMap(
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

  it should "use overrided object matcher when comparing set" in {
    implicit val lookupDiff: Diff[Startup] = Derived[Diff[Startup]]
      .modify(_.workers)
      .withSetMatcher[Person](ObjectMatcher.by(_.name))
    val p2m = p2.copy(age = 33)
    compare(Startup(Set(p1, p2)), Startup(Set(p1, p2m))) shouldBe DiffResultObject(
      "Startup",
      Map(
        "workers" -> DiffResultSet(
          List(
            IdenticalValue(p1),
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
}
