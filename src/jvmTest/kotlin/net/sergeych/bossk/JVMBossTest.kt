package net.sergeych.bossk

import kotlinx.datetime.Instant
import net.sergeych.mptools.decodeHex
import net.sergeych.mptools.encodeToHex
import net.sergeych.platform.runTest
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

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
                    .encodeToHex()
            )
            assertEquals(
                "36 00 61 69 11 21 33 68 65 6C 6C 6F 21", Bossk.pack(list)
                    .encodeToHex()
            )
            val iarray = ArrayList<Int>()
            iarray.add(10)
            iarray.add(20)
            iarray.add(1)
            iarray.add(2)
            assertEquals(iarray, Bossk.unpack(fromHex("26 50 A0 08 10")))
            assertEquals("26 50 A0 08 10", Bossk.pack(iarray).encodeToHex())
            val ba = byteArrayOf(0, 1, 2, 3, 4, 5)
            val bb = arrayOf(ba, ba)
            val x = Bossk.unpack<List<*>>(Bossk.pack(bb))
            assertEquals(2, x.size)
            assertContentEquals(ba, (x[0] as ByteArray))
            assertContentEquals(ba, (x[1] as ByteArray))
        }
    }

    @Test
    fun testConstants() {
        return runTest {
            assertEquals(0, Bossk.unpack<Any>(fromHex("00")) as Int)
            assertEquals("00", Bossk.pack(0).encodeToHex())
            assertEquals(true, Bossk.unpack(fromHex("61")))
            assertEquals("61", Bossk.pack(true).encodeToHex())
            assertEquals(false, Bossk.unpack(fromHex("69")))
            assertEquals("69", Bossk.pack(false).encodeToHex())
            assertEquals(1.0, Bossk.unpack(fromHex("11")), 1e-6)
            assertEquals("11", Bossk.pack(1.0).encodeToHex())
            assertEquals(-1.0, Bossk.unpack(fromHex("21")), 1e-6)
            assertEquals("21", Bossk.pack(-1.0).encodeToHex())
            assertEquals(0.0, Bossk.unpack<Any>(fromHex("09")) as Double, 1e-6)
            assertEquals("09", Bossk.pack(0.0).encodeToHex())
        }
    }


}