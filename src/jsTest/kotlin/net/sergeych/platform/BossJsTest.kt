package net.sergeych.platform

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.sergeych.boss_serialization.BossDecoder
import net.sergeych.boss_serialization_mp.BossEncoder
import net.sergeych.boss_serialization_mp.BossStruct
import net.sergeych.bossk.Bossk
import net.sergeych.mptools.truncateToSeconds
import org.khronos.webgl.Uint8Array
import kotlin.js.Date
import kotlin.test.Test
import kotlin.test.assertContentEquals

class BossJsTest {
    @Test
    fun testWriterArray() {
//            println("\n\n -------------------------------------------- \n\n\n")
            val w = Bossk.ByteArrayWriter()
            w.write(byteArrayOf(1, 2, 3))
            w.write(Uint8Array(arrayOf(4, 5, 6)))
//            println(w.toByteArray().toHex())
            assertContentEquals(byteArrayOf(28, 1, 2, 3, 28, 4, 5, 6), w.toByteArray())
//            println("\n\n -------------------------------------------- \n\n\n")
    }

    @Serializable
    data class Foobar(val x: Instant = Clock.System.now().truncateToSeconds())
    @Test
    fun testToFromNativeDate() {
        val a = Foobar()
        val encoded: BossStruct = BossEncoder.encodeToStruct(a)
        println(":: $encoded")
        println(":: ${encoded["x"]!!::class.simpleName}")
        encoded["x"] = Date(a.x.epochSeconds)
        println(":: $encoded")
        println(":: ${encoded["x"]!!::class.simpleName}")
        val b = BossDecoder.Companion.decodeFrom<Foobar>(encoded)

    }
}