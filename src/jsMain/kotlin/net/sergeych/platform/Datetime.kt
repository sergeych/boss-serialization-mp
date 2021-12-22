package net.sergeych.platform

import kotlinx.datetime.Instant
import kotlin.js.Date
import kotlin.math.roundToLong

actual fun convertToInstant(value: Any): Instant =
    when(value) {
        is Date -> Instant.fromEpochMilliseconds(value.getTime().roundToLong())
        is Instant -> value
        else -> throw IllegalArgumentException("Can't convert to instant: $value")
    }

actual fun convertToPlatformTime(instant: Instant): Any =
    Date(instant.toEpochMilliseconds())