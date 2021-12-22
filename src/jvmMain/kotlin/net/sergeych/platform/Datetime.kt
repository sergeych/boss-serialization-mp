package net.sergeych.platform

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneOffset
import java.time.ZonedDateTime

actual fun convertToInstant(value: Any): Instant =
    when(value) {
        is ZonedDateTime -> Instant.fromEpochSeconds(value.toEpochSecond())
        is java.time.Instant -> Instant.fromEpochMilliseconds(value.toEpochMilli())
        is Instant -> value
        else -> throw IllegalArgumentException("Can't convert to instant ${value::class.java.name}: $value")
    }

actual fun convertToPlatformTime(instant: Instant): Any
    = ZonedDateTime.ofInstant(instant.toJavaInstant(), ZoneOffset.UTC)