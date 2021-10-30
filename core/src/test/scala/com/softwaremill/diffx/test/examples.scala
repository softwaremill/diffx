package com.softwaremill.diffx.test

import java.time.Instant
import java.util.UUID

sealed trait ACoproduct
object ACoproduct {
  case class ProductA(id: String) extends ACoproduct
  case class ProductB(id: String) extends ACoproduct
}

case class Person(name: String, age: Int, in: Instant)

case class Family(first: Person, second: Person)

case class Organization(people: List[Person])

case class Startup(workers: Set[Person])

sealed trait Parent

case class Bar(s: String, i: Int) extends Parent

case class Foo(bar: Bar, b: List[Int], parent: Option[Parent]) extends Parent

class HasCustomEquals(val s: String) {
  override def equals(obj: Any): Boolean = {
    obj match {
      case o: HasCustomEquals => this.s.length == o.s.length
      case _                  => false
    }
  }
}

sealed trait TsDirection

object TsDirection {
  case object Incoming extends TsDirection

  case object Outgoing extends TsDirection
}

case class KeyModel(id: UUID, name: String)

case class MyLookup(map: Map[KeyModel, String])
