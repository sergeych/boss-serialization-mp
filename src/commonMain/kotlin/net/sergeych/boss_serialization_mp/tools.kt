@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package net.sergeych.boss_serialization_mp

import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.sergeych.boss_serialization.BossDecoder
import net.sergeych.bossk.Bossk

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
object ZonedDateTimeSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ZDT", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Instant) =
        encoder.encodeLong(value.epochSeconds)

    override fun deserialize(decoder: Decoder): Instant = Instant.fromEpochSeconds(decoder.decodeLong())
}


/**
 * Decode boss object from this binary data into a given class instance
 */
@OptIn(ExperimentalSerializationApi::class)
inline suspend fun <reified T> ByteArray.decodeBoss(): T = BossDecoder.decodeFrom(this)

/**
 * read and deserialize object from boss reader
 */
@Suppress("unused")
@OptIn(ExperimentalSerializationApi::class)
inline suspend fun <reified T> Bossk.Reader.deserialize(): T = BossDecoder.decodeFrom(this)!!

/**
 * ASCII dump representation for a binary data, with address, hex and ascii fields, following the
 * old tradition
 */
//fun ByteArray.dump(): String =
//    Bytes(this).toDump()
//
