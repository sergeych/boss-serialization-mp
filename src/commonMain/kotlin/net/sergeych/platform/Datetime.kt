package net.sergeych.platform

import kotlinx.datetime.Instant

expect fun convertToInstant(value: Any): Instant
expect fun convertToPlatformTime(instant: Instant): Any