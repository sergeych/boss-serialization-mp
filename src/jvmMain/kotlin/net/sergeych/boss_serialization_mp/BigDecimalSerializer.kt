package net.sergeych.boss_serialization_mp

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal

/**
 * De/serializer for java's BigDecimal. It is compatible with MP serializer [BigDecimalSerializerMp], so they could be
 * used tothether on different platforms (binaries packed with one are decodable with another).
 *
 * The usual way to use custom serializer is to declare it in the first line of your source file as:
 * ```
 * @file:UseSerializers(BigDecimalSerializer::class)
 * ~~~
 *
 * This implementation is compatible with universa standard for big decimals that urges to use strings in contract
 * decimal values. BE ready that not all contracts endorse it.
 */

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BGDC", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) =
        encoder.encodeString(value.toEngineeringString())

    override fun deserialize(decoder: Decoder): BigDecimal =
        BigDecimal(decoder.decodeString())
}

