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
      Map("name" -> Identical("<ignored>"), "age" -> DiffResultValue(22, 11), "in" -> Identical(instant))
    )
  }

  it should "allow importing and exporting implicits using macro on derived instance" in {
    implicit val d: Diff[Person] = Derived[Diff[Person]].ignore(_.name)
    compare(p1, p2) shouldBe DiffResultObject(
      "Person",
      Map("name" -> Identical("<ignored>"), "age" -> DiffResultValue(22, 11), "in" -> Identical(instant))
    )
  }

  it should "allow calling ignore multiple times" in {
    implicit val d: Diff[Person] = Derived[Diff[Person]]
      .ignore[Person, String](_.name)
      .ignore[Person, Int](_.age)
    compare(p1, p2) shouldBe Identical(p1)
  }

  it should "compare lists using explicit object matcher comparator" in {
    val o1 = Organization(List(p1, p2))
    val o2 = Organization(List(p2, p1))
    implicit val orgDiff: Diff[Organization] = Derived[Diff[Organization]]
      .modify[Organization, List[Person]](_.people)
      .withListMatcher(
        ObjectMatcher.byValue[Int, Person](ObjectMatcher.by(_.name))
      )
    compare(o1, o2) shouldBe Identical(Organization(List(p1, p2)))
  }

  it should "match map entries by values" in {
    implicit val lookupDiff: Diff[MyLookup] = Derived[Diff[MyLookup]]
      .modify[MyLookup, Map[KeyModel, String]](_.map)
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
                "name" -> Identical("k1")
              )
            ) -> Identical("val1")
          )
        )
      )
    )
  }

  it should "use overrided object matcher when comparing set" in {
    implicit val lookupDiff: Diff[Startup] = Derived[Diff[Startup]]
      .modify[Startup, Set[Person]](_.workers)
      .withSetMatcher[Person](ObjectMatcher.by(_.name))
    val p2m = p2.copy(age = 33)
    compare(Startup(Set(p1, p2)), Startup(Set(p1, p2m))) shouldBe DiffResultObject(
      "Startup",
      Map(
        "workers" -> DiffResultSet(
          List(
            Identical(p1),
            DiffResultObject(
              "Person",
              Map("name" -> Identical(p2.name), "age" -> DiffResultValue(p2.age, p2m.age), "in" -> Identical(p1.in))
            )
          )
        )
      )
    )
  }
}
