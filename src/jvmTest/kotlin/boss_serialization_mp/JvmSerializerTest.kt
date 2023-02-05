@file:UseSerializers(ZonedDateTimeSerializer::class, BigDecimalSerializer::class, BigDecimalSerializerMp::class)
@file:OptIn(ExperimentalSerializationApi::class)

package boss_serialization_mp

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.sergeych.boss_serialization.BossDecoder
import net.sergeych.boss_serialization_mp.*
import net.sergeych.mptools.toDump
import net.sergeych.platform.runTest
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class JvmSerializerTest {

//    @Serializable
//    data class FB(val foo: String,val bar: Int)

    @Serializable
    data class FBZ(val foo: String,val bar: Int,val dt: ZonedDateTime)

    @Test
    fun zonedDateTimeSupport() {
        return runTest {
            val x = FBZ("bazz", 42, ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS))
            val packed = BossEncoder.encodeToStruct(x)
//            println(packed)
            val dt = packed["dt"]!!
            assertIs<Instant>(dt)
//            println(BossEncoder.encode(x).toDump())
            val y = BossDecoder.decodeFrom<FBZ>(packed)
//            println(y)
            assertEquals(x, y)
            assertEquals(x, BossDecoder.decodeFrom<FBZ>(BossEncoder.encode(x)))
        }
    }

    @Serializable
    data class TestDecimals(val mp: BigDecimal,val j: java.math.BigDecimal)

    @Test
    fun javaBigintSerialization() {
        val mpx = BigDecimal.parseString("1.22")
        val jx = java.math.BigDecimal("1.22")
        val src = TestDecimals(mpx, jx)
        val packed = BossEncoder.encode(src)
//        val packed = BossEncoder.encode(TestMPDecimal(mpx))
//        println(packed.toDump())
        val u = packed.decodeBoss<TestDecimals>()
//        println("got $u")
        assertEquals(src, u)
    }

    @Serializable
    sealed class Status {
        @Serializable
        object Fooo: Status() {
            var bar: String = "123"
        }

        @Suppress("unused")
        @Serializable
        class Barr(val i: Int): Status()
    }

    @Test
    fun obejctSerializationTest() {
        val x: Status = Status.Fooo
        val encoded = BossEncoder.encode(x)
//        println(encoded.toDump())
//        println(": ${BossDecoder.decodeFrom<BossStruct>(encoded)}")
        val y = BossDecoder.decodeFrom<Status>(encoded)
//        println(y)
        assertIs<Status.Fooo>(y)
    }

    @Serializable
    data class Item(val foo: String,val bar: Int)


    @Test
    fun cacheTest() {
        val x = Item("bar", 42)
        println(BossEncoder.encode(x).toDump())
        println()
        val y = List(5) { x }
        println(BossEncoder.encode(y).toDump())
    }
    @Test
    fun cacheStringTest() {
        val x = "foo"
        println(BossEncoder.encode(x).toDump())
        println()
        val y = List(5) { x }
        println(BossEncoder.encode(y).toDump())
    }

    @Serializable
    data class OldTime(val t: Instant)
    @Test
    fun compatibilityTestOldTime() {
        val x = net.sergeych.boss.Boss.dump(
            net.sergeych.tools.Binder.of(
                "t", ZonedDateTime.now()
            )).toArray()
        println("${x.toDump()}")
        val t = BossDecoder.decodeFrom<OldTime>(x)
        println("--> $t")
    }
}