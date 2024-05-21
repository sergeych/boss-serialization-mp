package net.sergeych.bossk

import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get

/**
 * Dofferent platform may have specific ailas types for ByteArray. If the `source` *is* the sort of NyteArray (any
 * binary data we want to be treated by Boss as binary), convert them to the ByteArray.
 *
 * _important note_ on kotlin.js platform the result could be non-null and functionally analogous to ByteArray, bur
 * `(result is ByteArray) == false`. This is a platform featurebug and can't be converted without copying binary data
 * on the fly we try to avoid.
 *
 * @return converted binary data if the source is binary, null otherwise.
 */
actual fun convertArray(source: Any?): ByteArray? {
        return when(source) {
            is UByteArray -> return source.toByteArray()
            is ByteArray -> source
            is Uint8Array -> {
                val result = ByteArray(source.length)
                for( i in 0..<result.size) result[i] = source[i]
                result
            }
            is Int8Array -> {
                val result = ByteArray(source.length)
                for( i in 0..<result.size) result[i] = source.get(i)
                result
            }
            else -> null
        }
}