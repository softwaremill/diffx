package com.softwaremill.diffx.test

import com.softwaremill.diffx.{Diff, DiffResult}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time._
import java.util.UUID

class DiffxJvmInstancesTest extends AnyFreeSpec with Matchers {
  val cases = List(
    TestCase("LocalDate", LocalDate.now(), LocalDate.now().plusDays(1)),
    TestCase("LocalTime", LocalTime.now(), LocalTime.now().plusHours(1)),
    TestCase("LocalDateTime", LocalDateTime.now(), LocalDateTime.now().plusDays(1)),
    TestCase("OffsetDateTime", OffsetDateTime.now(), OffsetDateTime.now().plusDays(1)),
    TestCase("ZonedDateTime", ZonedDateTime.now(), ZonedDateTime.now().plusDays(1)),
    TestCase("Instant", Instant.now(), Instant.now().plusMillis(100)),
    TestCase("UUID", UUID.randomUUID(), UUID.randomUUID())
  )

  cases.foreach { tc =>
    tc.clazz - {
      s"identical ${tc.clazz}s should be identical" in {
        tc.compareIdentical.isIdentical shouldBe true
      }

      s"different ${tc.clazz}s should be different" in {
        tc.compareDifferent.isIdentical shouldBe false
      }
    }
  }

  case class TestCase[T: Diff](clazz: String, v1: T, v2: T) {
    def compareIdentical: DiffResult = Diff.compare(v1, v1)
    def compareDifferent: DiffResult = Diff.compare(v1, v2)
  }
}
