package net.sergeych.boss_serialization_mp

import net.sergeych.platform.TypeException
import net.sergeych.platform.unpackBytes

actual fun makeByteArray(source: Any): ByteArray {
    val result = unpackBytes<Any>(source)
    if( result is ByteArray ) return result
    throw TypeException("can;t convert to ByteArray: ${source::class.simpleName}")
}