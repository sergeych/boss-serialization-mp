package net.sergeych.bossk

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import net.sergeych.bossk.Bossk
import net.sergeych.mptools.decodeHex
import net.sergeych.mptools.flip
import net.sergeych.mptools.toHex
import net.sergeych.platform.runTest
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class JVMBossTest {

    fun fromHex(str: String) = str.decodeHex()

    @Test
    fun testConverter() {
        return runTest {
            // Boss limits time to seconds:
            val now = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS)
            val x = Bossk.packWith(ZonedDateTimeConverter, mapOf("now" to now))
            val t = Bossk.unpackWith<Map<String,ZonedDateTime>>(ZonedDateTimeConverter,x)["now"]
            assertEquals(now, t)
            val t2 = Bossk.unpack<Map<String,Instant>>(x)["now"]
            assertEquals(now.toEpochSecond(), t2?.epochSeconds)
        }
    }

    @Test
    fun testArrays() {
        // JVM target distinguish long and int from double, so the test for it is more precise
        // than, ay, for JVM:
        return runTest {
            val data: List<Any> = Arrays.asList(0, true, false, 1.0, -1.0, "hello!")
            assertEquals(
                data, Bossk.unpack(
                    fromHex("36 00 61 69 11 21 33 68 65 6C 6C 6F 21")
                )
            )
            val list = ArrayList<Any>()
            for (x in data) list.add(x)
            assertEquals(
                "36 00 61 69 11 21 33 68 65 6C 6C 6F 21", Bossk.pack(data)
                    .toHex()
            )
            assertEquals(
                "36 00 61 69 11 21 33 68 65 6C 6C 6F 21", Bossk.pack(list)
                    .toHex()
            )
            val iarray = ArrayList<Int>()
            iarray.add(10)
            iarray.add(20)
            iarray.add(1)
            iarray.add(2)
            assertEquals(iarray, Bossk.unpack(fromHex("26 50 A0 08 10")))
            assertEquals("26 50 A0 08 10", Bossk.pack(iarray).toHex())
            val ba = byteArrayOf(0, 1, 2, 3, 4, 5)
            val bb = arrayOf(ba, ba)
            val x = Bossk.unpack<List<*>>(Bossk.pack(bb))
            assertEquals(2, x.size)
            assertContentEquals(ba, (x[0] as ByteArray))
            assertContentEquals(ba, (x[1] as ByteArray))
        }
    }

}