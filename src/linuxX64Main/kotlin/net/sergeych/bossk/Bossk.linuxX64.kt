package net.sergeych.bossk

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
actual fun convertArray(source: Any?): ByteArray? = null