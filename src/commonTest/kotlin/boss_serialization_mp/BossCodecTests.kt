@file:OptIn(ExperimentalSerializationApi::class)

package boss_serialization_mp

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import net.sergeych.boss_serialization.BossDecoder
import net.sergeych.boss_serialization_mp.BossEncoder
import net.sergeych.boss_serialization_mp.decodeBoss
import net.sergeych.mptools.encodeToHex
import net.sergeych.mptools.toDump
import net.sergeych.mptools.truncateToSeconds
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.test.*

@Serializable
sealed class TBase

@Serializable
class TA(val a: Int) : TBase()

@Serializable
class TB(val b: String) : TBase()

@Serializable
class TTest(val list: List<TBase>)

@Serializable
sealed class UBase {
    @Serializable
    data class U1(val i: Int) : UBase()

    @Serializable
    data class U2(val s: String) : UBase()
}

@Serializable
sealed class VBase {
    @Serializable
    data class V1(val u: UBase, val b: Boolean) : VBase()

//    @Serializable
//    data class U2(val d: Double) : VBase()
}

//@Serializable
//class TCompound(
//    val tt: TTest,
//    val ss: String,
//)

internal class BossCodecTests {

//    @Serializable
//    data class FB(val foo: String,val bar: Int)

    @Serializable
    data class FBI(val foo: String, val bar: Int, val dt: Instant)

    @Test
    fun simpleCodec() {
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

    inline fun <reified T : Any> t(arg: T) {
        val x = BossEncoder.encodeToStruct(arg)
        try {
            if (BossDecoder.decodeFrom<T>(x) != arg) {
                println("t failed on $arg")
                println("encoded: $x")
                fail("failed to re-encode $arg")
            }
//            else {
//                println("OK: $x")
//            }
        } catch (e: Exception) {
            println("failed with exception on $arg")
            println("encoded: $x")
            throw e
        }
        tBinClass(arg)
    }

    inline fun <reified T : Any> tBinClass(arg: T) {
        val cls: KType = typeOf<T>()
        val x = BossEncoder.encode(cls, arg)
        try {
            val ok = when (arg) {
                is ByteArray -> BossDecoder.decodeFrom<ByteArray>(cls, x) contentEquals  arg
                else -> BossDecoder.decodeFrom<T>(cls, x) == arg
            }
            if (!ok) {
                println("b/cls t failed on $arg")
                println("encoded: ${x.toDump()} decoded: ${BossDecoder.decodeFrom<T>(cls, x)}")
                fail("failed to re-encode $arg")
            } else {
//                println("OK KType: $x")
            }
        } catch (e: Exception) {
            println("failed with exception on $arg")
            println("encoded: $x")
            throw e
        }
    }

    @Test
    fun serializeNested() {
        t(UBase.U1(42) as UBase)
        t(UBase.U2("foo") as UBase)
        t(VBase.V1(UBase.U1(422), true) as VBase)
    }

    @Test
    fun serializeExtented() {
//        tBinClass("hello")
//        tBinClass(byteArrayOf(1, 2, 3, 4, 5))
//        tBinClass(true)
//        tBinClass(false)
//        tBinClass(121)
//        tBinClass(1213121L)
//        tBinClass(11.7f)
        tBinClass(listOf(1,2,3))
    }

    @Test
    fun serializeSealed() {
        val x = TTest(listOf(TA(42), TB("Hello")))
        val b = BossEncoder.encode(x)
        val y = b.decodeBoss<TTest>()
        assertEquals(42, (y.list[0] as TA).a)
        assertEquals("Hello", (y.list[1] as TB).b)
    }

    @Test
    fun serializeRootList() {
        val x = listOf(TA(42), TB("Hello"))
        val b = BossEncoder.encode(x)
        val y = b.decodeBoss<List<TBase>>()
        assertEquals(42, (y[0] as TA).a)
        assertEquals("Hello", (y[1] as TB).b)
    }

    @Serializable
    class TBytes(val data: ByteArray)

    @Test
    fun toHex() {
        val x: Byte = -1
        assertEquals("FF", x.encodeToHex())
        val c = byteArrayOf(
            -62,
            43,
            111,
            -99,
            -120,
            46,
            12,
            -72,
            14,
            11,
            95,
            105,
            124,
            -1,
            -50,
            -8,
            -71,
            -65,
            -12,
            -120,
            116,
            122,
            30,
            66,
            40,
            71,
            -20,
            80,
            93,
            53,
            -76,
            -19
        )
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

    @Test
    fun serializeRootNull() {
        val x = BossEncoder.encode(null)
        val y = x.decodeBoss<TBase?>()
        assertNull(y)
    }

    @Test
    fun serializeSimpleTypes() {
        val x = BossEncoder.encode("hello")
        assertEquals("hello", x.decodeBoss<String>())
    }

    @Test
    fun serializeStingNullable() {
        val t = typeOf<String?>()
        var x: String? = "foo"
        var encoded = BossEncoder.encode(t, x)
        var y = BossDecoder.decodeFrom<String?>(t, encoded)
        assertEquals(x,y)

        x = null
        encoded = BossEncoder.encode(t, x)
        y = BossDecoder.decodeFrom<String?>(t, encoded)
        assertEquals(x,y)
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