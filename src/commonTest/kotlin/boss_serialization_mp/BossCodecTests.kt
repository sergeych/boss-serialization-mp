@file:OptIn(ExperimentalSerializationApi::class)

package boss_serialization_mp

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import net.sergeych.boss_serialization.BossDecoder
import net.sergeych.boss_serialization_mp.BossEncoder
import net.sergeych.boss_serialization_mp.decodeBoss
import net.sergeych.mptools.toDump
import net.sergeych.mptools.toHex
import net.sergeych.mptools.truncateToSeconds
import net.sergeych.platform.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

import kotlin.test.assertIs
import kotlin.test.assertNull

@Serializable
sealed class TBase

@Serializable
class TA(val a: Int): TBase()

@Serializable
class TB(val b: String): TBase()

@Serializable
class TTest(val list: List<TBase>)

internal class BossCodecTests {

//    @Serializable
//    data class FB(val foo: String,val bar: Int)

    @Serializable
    data class FBI(val foo: String,val bar: Int,val dt: Instant)

    @Test
    fun simpleCodec() {
        return runTest {
            val x = FBI("bazz", 42, Clock.System.now().truncateToSeconds())
            val packed = BossEncoder.encodeToStruct(x)
//            println(packed)
            val dt = packed["dt"]!!
            assertIs<Instant>(dt)
//            println(BossEncoder.encode(x).toDump())
            val y = BossDecoder.decodeFrom<FBI>(packed)
//            println(y)
            assertEquals(x, y)
            assertEquals(x, BossDecoder.decodeFrom<FBI>(BossEncoder.encode(x)))
        }
    }

    @Test
    fun serializeSealed() {
        return runTest {
            val x = TTest(listOf(TA(42), TB("Hello")))
            val b = BossEncoder.encode(x)
            println(b.toDump())
            val y = b.decodeBoss<TTest>()
            println(y.list[0])
            println(y.list[1])
            assertEquals(42, (y.list[0] as TA).a)
            assertEquals("Hello", (y.list[1] as TB).b)
        }
    }

    @Test
    fun serializeRootList() {
        return runTest {
            val x = listOf(TA(42), TB("Hello"))
            val b = BossEncoder.encode(x)
            println(b.toDump())
            val y = b.decodeBoss<List<TBase>>()
            println(y[0])
            println(y[1])
            assertEquals(42, (y[0] as TA).a)
            assertEquals("Hello", (y[1] as TB).b)
        }
    }

    @Serializable
    class TBytes(val data: ByteArray)

    @Test
    fun toHex() {
        val x: Byte = -1
        assertEquals("FF", x.toHex())
        val c = byteArrayOf(-62,43,111,-99,-120,46,12,-72,14,11,95,105,124,-1,-50,-8,-71,-65,-12,-120,116,122,30,66,40,71,-20,80,93,53,-76,-19)
        assertEquals("{data=|C2 2B 6F 9D 88 2E 0C…(32)|}", BossEncoder.encodeToStruct(TBytes(c)).toString())
    }

//    @Test
//    fun serializeToStructRootList() {
//        return runTest {
//            val x = listOf(TA(42), TB("Hello"))
//            val b = BossEncoder.encodeToStruct(x)
//            println(b)
//            val y = b.decodeBoss<List<TBase>>()
//            println(y[0])
//            println(y[1])
//            assertEquals(42, (y[0] as TA).a)
//            assertEquals("Hello", (y[1] as TB).b)
//        }
//    }

    @Test fun serializeRootNull() {
        return runTest {
            val x = BossEncoder.encode(null)
            println(x.toDump())
            val y = x.decodeBoss<TBase?>()
            assertNull(y)
        }
    }

    // Not sure whether it is actually needed?
//    @Test fun serializeListOrStruct() {
//        return runTest {
//            val a = listOf<Any?>("foo", null, TB("bar"))
//            val x = BossEncoder.encode(a)
//            println(x.toDump())
//            val y = x.decodeBoss<TBase?>()
//            assertNull(y)
//        }
//    }
}