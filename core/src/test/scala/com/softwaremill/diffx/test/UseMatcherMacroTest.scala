package com.softwaremill.diffx.test

import org.scalatest.freespec.AnyFreeSpec

class UseMatcherMacroTest extends AnyFreeSpec {
  case class Organization(people: Set[Person])

  "should compile when using with set matcher" in {
    assertCompiles("""import com.softwaremill.diffx.generic.auto._
        |import com.softwaremill.diffx.{Diff, ObjectMatcher}
        |
        |case class Organization(people: Set[Person])
        |case class Person(name: String, age: Int)
        |
        |Diff[Organization].modify(_.people).useMatcher(ObjectMatcher.set[Person].by(_.name))
        |""".stripMargin)
  }

  "should compile when using with list matcher" in {
    assertCompiles("""import com.softwaremill.diffx.generic.auto._
        |import com.softwaremill.diffx.{Diff, ObjectMatcher}
        |
        |case class Organization(people: List[Person])
        |case class Person(name: String, age: Int)
        |
        |Diff[Organization].modify(_.people).useMatcher(ObjectMatcher.list[Person].byValue(_.name))
        |""".stripMargin)
  }

  "should compile when using with map matcher" in {
    assertCompiles("""import com.softwaremill.diffx.generic.auto._
        |import com.softwaremill.diffx.{Diff, ObjectMatcher}
        |
        |case class Organization(people: Map[String, Person])
        |case class Person(name: String, age: Int)
        |
        |Diff[Organization].modify(_.people).useMatcher(ObjectMatcher.map[String, Person].byValue(_.name))
        |""".stripMargin)
  }
}
