package net.sergeych.bossk

import org.khronos.webgl.Uint8Array

/**
 * in JS Platform Uint8Array is treated the same as ByteArray, though the "is" operator treats them
 * differently, so we just unsafe cast it.
 */
actual fun convertArray(source: Any?): ByteArray? {
    return if (source is Uint8Array)
        source.unsafeCast<ByteArray>()
    else
        null
}