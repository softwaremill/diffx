package com.softwaremill.diffx

import java.time._
import java.util.UUID

trait DiffxPlatformExtensions {
  implicit val diffUuid: Diff[UUID] = Diff.fallback

  implicit val diffInstant: Diff[Instant] = Diff.fallback
  implicit val diffLocalDate: Diff[LocalDate] = Diff.fallback
  implicit val diffLocalTime: Diff[LocalTime] = Diff.fallback
  implicit val diffLocalDateTime: Diff[LocalDateTime] = Diff.fallback
  implicit val diffOffsetDateTime: Diff[OffsetDateTime] = Diff.fallback
  implicit val diffZonedDateTime: Diff[ZonedDateTime] = Diff.fallback
}
