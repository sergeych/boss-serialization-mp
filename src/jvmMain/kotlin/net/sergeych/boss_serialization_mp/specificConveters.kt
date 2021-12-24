@file:UseSerializers(ZonedDateTimeSerializer::class)

package net.sergeych.boss_serialization

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.SerialDescriptor
import net.sergeych.boss_serialization_mp.ZonedDateTimeSerializer
import java.time.ZoneId
import java.time.ZonedDateTime

class ZonedDateTimePlatofrmConverter : SpecificConverter {
    override val descriptor: SerialDescriptor
        get() = ZonedDateTimeSerializer.descriptor

    override fun serialize(source: Any): Any = Instant.fromEpochSeconds((source as ZonedDateTime).toEpochSecond())

    override fun deserialize(bossPacked: Any): Any =
        ZonedDateTime.ofInstant((bossPacked as Instant).toJavaInstant(), ZoneId.systemDefault())

}

actual val platfrmSpecificConverters: Iterable<SpecificConverter> = listOf(ZonedDateTimePlatofrmConverter())