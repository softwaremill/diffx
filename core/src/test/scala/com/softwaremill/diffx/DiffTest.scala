package com.softwaremill.diffx

import java.time.Instant
import java.util.UUID

import org.scalatest.{FunSpec, Matchers}

class DiffTest extends FunSpec with Matchers {

  private val instant: Instant = Instant.now()
  val p1 = Person("p1", 22, instant)
  val p2 = Person("p2", 11, instant)

  it("calculate diff for simple value") {
    compare(1, 2) shouldBe DiffResultValue(1, 2)
    compare(1, 1) shouldBe Identical(1)
  }

  it("calculate diff for product types") {
    compare(p1, p2) shouldBe DiffResultObject(
      "Person",
      Map(
        "name" -> DiffResultValue(p1.name, p2.name),
        "age" -> DiffResultValue(p1.age, p2.age),
        "in" -> Identical(instant)
      )
    )
  }

  it("calculate diff for product types ignoring given fields") {
    val d = Diff[Person].ignoreUnsafe("name").ignoreUnsafe("age")
    val p3 = p2.copy(in = Instant.now())
    compare(p1, p3)(d) shouldBe DiffResultObject(
      "Person",
      Map(
        "name" -> Identical(p1.name),
        "age" -> Identical(p1.age),
        "in" -> DiffResultValue(p1.in, p3.in)
      )
    )
  }

  it("calculate identity for product types") {
    compare(p1, p1) shouldBe Identical(p1)
  }

  it("calculate diff for nested products") {
    val f1 = Family(p1, p2)
    val f2 = Family(p1, p1)
    compare(f1, f2) shouldBe DiffResultObject(
      "Family",
      Map(
        "first" -> Identical(p1),
        "second" -> DiffResultObject(
          "Person",
          Map(
            "name" -> DiffResultValue(p2.name, p1.name),
            "age" -> DiffResultValue(p2.age, p1.age),
            "in" -> Identical(instant)
          )
        )
      )
    )
  }

  it("calculate diff for nested products ignoring nested fields") {
    val f1 = Family(p1, p2)
    val f2 = Family(p1, p1)
    val d = Diff[Family].ignoreUnsafe("second", "name")
    compare(f1, f2)(d) shouldBe DiffResultObject(
      "Family",
      Map(
        "first" -> Identical(p1),
        "second" -> DiffResultObject(
          "Person",
          Map(
            "name" -> Identical(p2.name),
            "age" -> DiffResultValue(p2.age, p1.age),
            "in" -> Identical(instant)
          )
        )
      )
    )
  }

  it("calculate diff for nested products ignoring fields only in given path") {
    val p1p = p1.copy(name = "other")
    val f1 = Family(p1, p2)
    val f2 = Family(p1p, p2.copy(name = "other"))
    val d = Diff[Family].ignoreUnsafe("second", "name")
    compare(f1, f2)(d) shouldBe DiffResultObject(
      "Family",
      Map(
        "first" -> DiffResultObject(
          "Person",
          Map(
            "name" -> DiffResultValue(p1.name, p1p.name),
            "age" -> Identical(p1.age),
            "in" -> Identical(instant)
          )
        ),
        "second" -> Identical(p2)
      )
    )
  }

  it("calculate diff for nested products ignoring nested products") {
    val f1 = Family(p1, p2)
    val f2 = Family(p1, p1)
    val d = Diff[Family].ignoreUnsafe("second")
    compare(f1, f2)(d) shouldBe Identical(f1)
  }

  it("calculate diff for iterables") {
    val o1 = Organization(List(p1, p2))
    val o2 = Organization(List(p1, p1, p1))
    compare(o1, o2) shouldBe DiffResultObject(
      "Organization",
      Map(
        "people" -> DiffResultObject(
          "List",
          Map(
            "0" -> Identical(p1),
            "1" -> DiffResultObject(
              "Person",
              Map(
                "name" -> DiffResultValue(p2.name, p1.name),
                "age" -> DiffResultValue(p2.age, p1.age),
                "in" -> Identical(instant)
              )
            ),
            "2" -> DiffResultMissing(Person(p1.name, p1.age, instant))
          )
        )
      )
    )
  }

  it("calculate diff for sealed trait objects") {
    compare[TsDirection](TsDirection.Outgoing, TsDirection.Incoming) shouldBe DiffResultValue(
      "com.softwaremill.diffx.TsDirection.Outgoing",
      "com.softwaremill.diffx.TsDirection.Incoming"
    )
  }

  val right: Foo = Foo(
    Bar("asdf", 5),
    List(123, 1234),
    Some(Bar("asdf", 5))
  )
  val left: Foo = Foo(
    Bar("asdf", 66),
    List(1234),
    Some(right)
  )

  it("calculate diff for coproduct types") {
    compare(left, right) shouldBe DiffResultObject(
      "Foo",
      Map(
        "bar" -> DiffResultObject("Bar", Map("s" -> Identical("asdf"), "i" -> DiffResultValue(66, 5))),
        "b" -> DiffResultObject("List", Map("0" -> DiffResultValue(1234, 123), "1" -> DiffResultMissing(1234))),
        "parent" -> DiffResultValue("com.softwaremill.diffx.Foo", "com.softwaremill.diffx.Bar")
      )
    )

  }

  describe("diff for list") {
    it("use ignored fields from elements") {
      val o1 = Organization(List(p1, p2))
      val o2 = Organization(List(p1, p1, p1))
      val d = Diff[Organization].ignoreUnsafe("people", "name")
      compare(o1, o2)(d) shouldBe DiffResultObject(
        "Organization",
        Map(
          "people" -> DiffResultObject(
            "List",
            Map(
              "0" -> Identical(p1),
              "1" -> DiffResultObject(
                "Person",
                Map(
                  "name" -> Identical(p2.name),
                  "age" -> DiffResultValue(p2.age, p1.age),
                  "in" -> Identical(instant)
                )
              ),
              "2" -> DiffResultMissing(Person(p1.name, p1.age, instant))
            )
          )
        )
      )
    }

    it("consider same lists as identical") {
      compare(List("a"), List("a")) shouldBe Identical(List("a"))
    }

    it("compare lists using set-like comparator") {
      val o1 = Organization(List(p1, p2))
      val o2 = Organization(List(p2, p1))
      implicit val om: ObjectMatcher[Person] = (left: Person, right: Person) => left.name == right.name
      implicit val dd: Derived[Diff[List[Person]]] = new Derived(Diff[Set[Person]].contramap(_.toSet))
      compare(o1, o2) shouldBe Identical(Organization(List(p1, p2)))
    }
  }
  describe("diff for sets") {
    it("calculate diff for sets") {
      val diffResult = compare(Set(1, 2, 3, 4, 5), Set(1, 2, 3, 4)).asInstanceOf[DiffResultSet]
      diffResult.diffs should contain theSameElementsAs List(
        DiffResultAdditional(5),
        Identical(4),
        Identical(3),
        Identical(2),
        Identical(1)
      )
    }
    it("calculate diff using ignored fields from elements") {
      val p2m = p2.copy(age = 33, in = Instant.now())
      val d = Diff[Person].ignoreUnsafe("age")
      implicit val im: ObjectMatcher[Person] = (left: Person, right: Person) => left.name == right.name
      val ds: Derived[Diff[Set[Person]]] = Diff.diffForSet(im, Derived(d), implicitly[ObjectMatcher[Person]])
      compare(Set(p1, p2), Set(p1, p2m))(ds.value) shouldBe DiffResultSet(
        List(
          Identical(p1),
          DiffResultObject(
            "Person",
            Map(
              "name" -> Identical(p2.name),
              "age" -> Identical(p2.age),
              "in" -> DiffResultValue(p1.in, p2m.in)
            )
          )
        )
      )
    }

    it("work also for mutable variant") {
      import scala.collection.{Set => mSet}
      val diffResult = compare(mSet(1, 2, 3, 4, 5), mSet(1, 2, 3, 4)).asInstanceOf[DiffResultSet]
      diffResult.diffs should contain theSameElementsAs List(
        DiffResultAdditional(5),
        Identical(4),
        Identical(3),
        Identical(2),
        Identical(1)
      )
    }

    it("be identical when products are identical using ignored") {
      val p2m = p2.copy(age = 33, in = Instant.now())
      val d = Diff[Person].ignoreUnsafe("age").ignoreUnsafe("in")
      val ds: Derived[Diff[Set[Person]]] =
        Diff.diffForSet(implicitly[ObjectMatcher[Person]], Derived(d), implicitly[ObjectMatcher[Person]])
      compare(Set(p1, p2), Set(p1, p2m))(ds.value) shouldBe Identical(Set(p1, p2))
    }

    it("propagate ignore fields to elements") {
      val p2m = p2.copy(in = Instant.now())
      implicit val im: ObjectMatcher[Person] = (left: Person, right: Person) => left.name == right.name
      val ds: Diff[Set[Person]] =
        Diff.diffForSet(im, Derived[Diff[Person]], implicitly[ObjectMatcher[Person]]).value.ignoreUnsafe("age")
      compare(Set(p1, p2), Set(p1, p2m))(ds) shouldBe DiffResultSet(
        List(
          Identical(p1),
          DiffResultObject(
            "Person",
            Map(
              "name" -> Identical(p2.name),
              "age" -> Identical(p2.age),
              "in" -> DiffResultValue(p1.in, p2m.in)
            )
          )
        )
      )
    }
    it("calculate diff for set of products") {
      val p2m = p2.copy(age = 33)
      compare(Set(p1, p2), Set(p1, p2m)) shouldBe DiffResultSet(
        List(DiffResultAdditional(p2), DiffResultMissing(p2m), Identical(p1))
      )
    }

    it("calculate diff for set of products using instance matcher") {
      val p2m = p2.copy(age = 33)
      implicit val im: ObjectMatcher[Person] = (left: Person, right: Person) => left.name == right.name
      compare(Set(p1, p2), Set(p1, p2m)) shouldBe DiffResultSet(
        List(
          Identical(p1),
          DiffResultObject(
            "Person",
            Map("name" -> Identical(p2.name), "age" -> DiffResultValue(p2.age, p2m.age), "in" -> Identical(p1.in))
          )
        )
      )
    }
  }

  describe("diff for maps") {
    it("propagate ignored fields to elements") {
      val dm = Diff[Map[String, Person]].ignoreUnsafe("age")
      compare(Map("first" -> p1), Map("first" -> p2))(dm) shouldBe DiffResultMap(
        Map(
          Identical("first") -> DiffResultObject(
            "Person",
            Map(
              "name" -> DiffResultValue(p1.name, p2.name),
              "age" -> Identical(p1.age),
              "in" -> Identical(p1.in)
            )
          )
        )
      )
    }

    it("be identical when products are identical using ignore") {
      val dm = Diff[Map[String, Person]].ignoreUnsafe("age").ignoreUnsafe("name")
      compare(Map("first" -> p1), Map("first" -> p2))(dm) shouldBe Identical(Map("first" -> p1))
    }

    it("compare maps by values") {
      type DD[T] = Derived[Diff[T]]

      implicit def mapWithoutKeys[T, R: DD]: Derived[Diff[Map[T, R]]] =
        new Derived(Diff[List[R]].contramap(_.values.toList))

      val person = Person("123", 11, Instant.now())
      compare(
        Map[String, Person]("i1" -> person),
        Map[String, Person]("i2" -> person)
      ) shouldBe Identical(List(person))
    }

    it("ignore part of map's key using keys's diff specification") {
      implicit def dm: Diff[KeyModel] = Derived[Diff[KeyModel]].ignore(_.id)

      val a1 = MyLookup(Map(KeyModel(UUID.randomUUID(), "k1") -> "val1"))
      val a2 = MyLookup(Map(KeyModel(UUID.randomUUID(), "k1") -> "val1"))
      compare(a1, a2) shouldBe Identical(a1)
    }

    it("should match values using object mapper") {
      implicit val om: ObjectMatcher[KeyModel] = new ObjectMatcher[KeyModel] {
        override def isSameObject(left: KeyModel, right: KeyModel): Boolean = left.name == right.name
      }
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
  }

  private def compare[T](t1: T, t2: T)(implicit d: Diff[T]) = d.apply(t1, t2)
}

case class Person(name: String, age: Int, in: Instant)

case class Family(first: Person, second: Person)

case class Organization(people: List[Person])

sealed trait Parent

case class Bar(s: String, i: Int) extends Parent

case class Foo(bar: Bar, b: List[Int], parent: Option[Parent]) extends Parent

sealed trait TsDirection

object TsDirection {

  case object Incoming extends TsDirection

  case object Outgoing extends TsDirection

}

case class KeyModel(id: UUID, name: String)

case class MyLookup(map: Map[KeyModel, String])
