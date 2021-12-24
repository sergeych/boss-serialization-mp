package net.sergeych.mptools

import kotlinx.datetime.Instant

fun Instant.truncateToSeconds(): Instant = Instant.fromEpochSeconds(this.epochSeconds)