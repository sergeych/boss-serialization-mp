package net.sergeych.boss_serialization_mp

import net.sergeych.platform.TypeException
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

// in javascript byte array is always byte array (UInt8Array)
//actual fun makeByteArray(it: Any): ByteArray = it as ByteArray

actual fun makeByteArray(source: Any): ByteArray =
    when(source) {
        is ByteArray -> source
        is Uint8Array -> source as ByteArray
        is ArrayBuffer -> Uint8Array(source, 0, source.byteLength) as ByteArray
        else -> throw TypeException("can't convert to ByteArray: ${source::class.simpleName}")
    }

