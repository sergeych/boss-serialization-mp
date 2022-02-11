package net.sergeych.boss_serialization_mp

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * De/serializer for MP-capable [com.ionspin.kotlin.bignum.decimal.BigDecimal](https://github.com/ionspin/kotlin-multiplatform-bignum).
 * This library is already included in dependencies, so we recommend to use it. Also, on Java platform, we provide
 * cross-compatible serializer for `java.math.BigDecimal`.
 *
 * The usual way to use custom serializer is to declare it in the first line of your source file as:
 * ```
 * @file:UseSerializers(BigDecimalSerializerMp::class)
 * ~~~
 *
 * This implementation is compatible with universa standard for big decimals that urges to use strings in contract
 * decimal values. BE ready that not all contracts endorse it.
 */
object BigDecimalSerializerMp : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BGDC", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): BigDecimal =
        BigDecimal.parseString(decoder.decodeString())
}

