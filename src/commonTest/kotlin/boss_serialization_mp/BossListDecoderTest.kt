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
import net.sergeych.mptools.truncateToSeconds
import net.sergeych.platform.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

import kotlin.test.assertIs

@Serializable
sealed class TBase

@Serializable
class TA(val a: Int): TBase()

@Serializable
class TB(val b: String): TBase()

@Serializable
class TTest(val list: List<TBase>)

internal class BossListDecoderTest {

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
}