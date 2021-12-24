package net.sergeych.boss_serialization_mp

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Serialization for ZonedDateTime. In Boss serialization, it falls back to Boss-native datetime type, otherwise
 * serializes to long with unix epoch second. The time is always truncated to second (as stated, unix
 * epoch second).
 *
 * Please use this serializer explicitly, for example by adding
 * ```
 * @file:UseSerializers(ZonedDateTimeSerializer::class)
 * ```
 * to the top of any file that serializes `ZonedDateTime`. Using any other serializer for this type
 * may break binary compatibility with other Boss consumers.
 */
object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ZDT", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: ZonedDateTime) =
        encoder.encodeLong(value.toEpochSecond())

    override fun deserialize(decoder: Decoder): ZonedDateTime =
        ZonedDateTime.ofInstant(Instant.ofEpochSecond(decoder.decodeLong()), ZoneId.systemDefault())
}

