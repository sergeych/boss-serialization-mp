package net.sergeych.bossk

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Allows usage of `ZonedDateTime` with bossk protocol as low-level primitive. By default it uses
 * `kotlinx.datetime.Instant`.
 */
object ZonedDateTimeConverter : Bossk.Converter {
    override fun toBoss(source: Any?): Any? = when(source) {
        is ZonedDateTime -> Instant.fromEpochSeconds(source.toEpochSecond())
        else -> source
    }

    override fun fromBoss(packed: Any?): Any? = when(packed) {
        is Instant -> ZonedDateTime.ofInstant(packed.toJavaInstant(), ZoneId.systemDefault())
        else -> packed
    }
}