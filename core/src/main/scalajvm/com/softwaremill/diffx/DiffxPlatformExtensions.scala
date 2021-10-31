package com.softwaremill.diffx

import java.time._
import java.util.UUID

trait DiffxPlatformExtensions {
  implicit val diffUuid: Diff[UUID] = Diff.useEquals

  implicit val diffInstant: Diff[Instant] = Diff.useEquals
  implicit val diffLocalDate: Diff[LocalDate] = Diff.useEquals
  implicit val diffLocalTime: Diff[LocalTime] = Diff.useEquals
  implicit val diffLocalDateTime: Diff[LocalDateTime] = Diff.useEquals
  implicit val diffOffsetDateTime: Diff[OffsetDateTime] = Diff.useEquals
  implicit val diffZonedDateTime: Diff[ZonedDateTime] = Diff.useEquals
}
