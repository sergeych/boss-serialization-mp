package net.sergeych.bossk

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import net.sergeych.bossk.Bossk
import net.sergeych.mptools.decodeHex
import net.sergeych.mptools.flip
import net.sergeych.mptools.toHex
import java.util.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

fun BigInteger(text: String): BigInteger = BigInteger.parseString(text)

internal class JVMBossTest {

    @Test
    fun packUnpack() {
        return runBlocking {
            val x = Bossk.pack("Hello")
            assertEquals("Hello", Bossk.unpack(x))
            val y = Bossk.pack(mapOf("foo" to 42, "bar" to "buzz"))
            println("${Bossk.unpack<Any>(y)}")
        }
    }

    fun bytesFromHex(str: String) = str.decodeHex()

    @Test
    fun testBigIntegers() {
        return runBlocking {
            val bi = BigInteger("97152833356252188945")
            println("> $bi")
            println("! F8 89 11 11 22 22 33 33 44 44 05")
            println("> -- -- ${bi.toByteArray().flip().toHex()}")
            println("< ${Bossk.pack(bi).toHex()}")
            assertEquals(
                "F8 89 11 11 22 22 33 33 44 44 05",
                Bossk.pack(BigInteger.parseString("97152833356252188945")).toHex()
            )
            assertEquals(
                BigInteger.parseString("97152833356252188945"),
                Bossk.unpack(bytesFromHex("F8 89 11 11 22 22 33 33 44 44 05"))
            )
            assertEquals(
                "F8 89 11 11 22 22 33 33 44 44 05",
                Bossk.pack(BigInteger.parseString("97152833356252188945")).toHex()
            )
            assertEquals(
                BigInteger.parseString("-97152833356252188945"),
                Bossk.unpack(bytesFromHex("FA 89 11 11 22 22 33 33 44 44 05"))
            )
            assertEquals(
                "FA 89 11 11 22 22 33 33 44 44 05",
                Bossk.pack(BigInteger.parseString("-97152833356252188945")).toHex()
            )

        }
    }

    @Test
    fun testIntegers() {
        return runBlocking {
            assertEquals(7, (Bossk.unpack<Any>("38".decodeHex()) as Number).toInt())
            assertContentEquals(bytesFromHex("38"), Bossk.pack(7))
            assertEquals(17, Bossk.unpack<Any>("88".decodeHex()) as Int)
            assertContentEquals(bytesFromHex("88"), Bossk.pack(17))
            assertEquals(99, Bossk.unpack<Any>(bytesFromHex("B8 63")) as Int)
            assertEquals("B8 63", Bossk.pack(99).toHex())
            assertEquals(331, Bossk.unpack<Any>(bytesFromHex("C0 4B 01")) as Int)
            assertEquals("C0 4B 01", Bossk.pack(331).toHex())
            assertEquals(-7, Bossk.unpack<Any>(bytesFromHex("3A")) as Int)
            assertEquals("3A", Bossk.pack(-7).toHex())
            assertEquals(-17, Bossk.unpack<Any>(bytesFromHex("8A")) as Int)
            assertEquals("8A", Bossk.pack(-17).toHex())
            assertEquals(-99, Bossk.unpack<Any>(bytesFromHex("BA 63")) as Int)
            assertEquals("BA 63", Bossk.pack(-99).toHex())
            assertEquals(-331, Bossk.unpack<Any>(bytesFromHex("C2 4B 01")) as Int)
            assertEquals("C2 4B 01", Bossk.pack(-331).toHex())
            assertEquals(
                13457559825L,
                Bossk.unpack<Any>(bytesFromHex("D8 11 11 22 22 03")) as Long
            )
            assertEquals("D8 11 11 22 22 03", Bossk.pack(13457559825L).toHex())
            assertEquals(
                -13457559825L,
                Bossk.unpack<Any>(bytesFromHex("DA 11 11 22 22 03")) as Long
            )
            assertEquals("DA 11 11 22 22 03", Bossk.pack(-13457559825L).toHex())
            assertEquals(
                4919112987704430865L,
                Bossk.unpack<Any>(bytesFromHex("F0 11 11 22 22 33 33 44 44")) as Long
            )
            assertEquals(
                "F0 11 11 22 22 33 33 44 44",
                Bossk.pack(4919112987704430865L).toHex()
            )
            assertEquals(
                -4919112987704430865L,
                Bossk.unpack<Any>(bytesFromHex("F2 11 11 22 22 33 33 44 44")) as Long
            )
            assertEquals(
                "F2 11 11 22 22 33 33 44 44",
                Bossk.pack(-4919112987704430865L).toHex()
            )
            assertEquals("B0", Bossk.pack(22).toHex())
            assertEquals("B8 17", Bossk.pack(23).toHex())
            assertEquals("B0", Bossk.pack(22).toHex())
            assertEquals(22, Bossk.unpack<Any>("B0".decodeHex()) as Int)
            assertEquals(23, Bossk.unpack<Any>("B8 17".decodeHex()) as Int)
            for (i in 0..799) {
                assertEquals(i, Bossk.unpack<Any>(Bossk.pack(i)) as Int)
                assertEquals(-i, Bossk.unpack<Any>(Bossk.pack(-i)) as Int)
            }
            assertEquals("B8 1E", Bossk.pack(30).toHex())
            assertEquals("B8 1F", Bossk.pack(31).toHex())
        }
    }

    @Test
    fun testStringsAndBinaries() {
        return runBlocking {
            assertEquals("Hello", Bossk.unpack("2B 48 65 6C 6C 6F".decodeHex()))
            assertEquals("2B 48 65 6C 6C 6F", Bossk.pack("Hello").toHex())
            val bb = "00 01 02 03 04 05".decodeHex()
            val rr = Bossk.unpack<ByteArray>("34 00 01 02 03 04 05".decodeHex())
            assertEquals(bb.toHex(), rr.toHex())
            val encoded = Bossk.pack(rr)
            assertEquals("34 00 01 02 03 04 05", encoded.toHex())
            val ba = byteArrayOf(0, 1, 2, 3, 4, 5)
            assertEquals("34 00 01 02 03 04 05", Bossk.pack(ba).toHex())

            // Should pach utf8
            assertEquals("Абвгд", Bossk.unpack(Bossk.pack("Абвгд")))
        }
    }

    fun fromHex(s: String) = s.decodeHex()

    @Test
    fun testConstants() {
        return runBlocking {
            assertEquals(0, Bossk.unpack<Any>(fromHex("00")) as Int)
            assertEquals("00", Bossk.pack(0).toHex())
            assertEquals(true, Bossk.unpack(fromHex("61")))
            assertEquals("61", Bossk.pack(true).toHex())
            assertEquals(false, Bossk.unpack(fromHex("69")))
            assertEquals("69", Bossk.pack(false).toHex())
            assertEquals(1.0, Bossk.unpack(fromHex("11")), 1e-6)
            assertEquals("11", Bossk.pack(1.0).toHex())
            assertEquals(-1.0, Bossk.unpack(fromHex("21")), 1e-6)
            assertEquals("21", Bossk.pack(-1.0).toHex())
            assertEquals(0.0, Bossk.unpack<Any>(fromHex("09")) as Double, 1e-6)
            assertEquals("09", Bossk.pack(0.0).toHex())
        }
    }

    @Test
    fun testArrays() {
        return runBlocking {
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

    @Test
    fun testHashes() {
        return runBlocking {
            var res = Bossk.unpack<Map<Any, Any>>(fromHex("1F 1B 6F 6E 65 1B 74 77 6F 2B 47 72 65 61 74 61 B8 AC 69"))
            assertEquals(res.size, 3)
            assertEquals(res["one"], "two")
            assertEquals(res["Great"], true)
            assertEquals(res[172] as Boolean, false)
            res = Bossk.unpack(Bossk.pack(res))
            assertEquals(res.size, 3)
            assertEquals(res["one"], "two")
            assertEquals(res["Great"], true)
            assertEquals(res.get(172), false)
        }
    }

    @Test
    fun testDate() {
        return runBlocking {
            val date = Bossk.unpack<Instant>(fromHex("79 2A 24 0E 10 85"))
            assertNotNull(date)
            assertEquals(date.epochSeconds, 1375965738L)
            assertEquals("79 2A 24 0E 10 85", Bossk.pack(date).toHex())
        }
    }


    @Test
    fun testDouble() {
        return runBlocking {
            assertEquals(17.37e-111, Bossk.unpack<Double>(fromHex("39 3C BD FC B1 F9 E2 24 29")), 1e-6)
            assertEquals("39 3C BD FC B1 F9 E2 24 29", Bossk.pack(17.37e-111).toHex())
        }
    }
//    @Test
//    fun testHashes() {
//        return runBlocking {
//            var res = Boss
//                .load<Any>(fromHex("1F 1B 6F 6E 65 1B 74 77 6F 2B 47 72 65 61 74 61 B8 AC 69")) as BossStruct
//            assertEquals(res.size, 3)
//            assertEquals(res["one"], "two")
//            assertEquals(res["Great"], true)
//            assertEquals(res.get(172), false)
//            res = Boss.load<BossStruct>(Boss.dump(res))
//            assertEquals(res.size, 3)
//            assertEquals(res["one"], "two")
//            assertEquals(res["Great"], true)
//            assertEquals(res.getAs(172), false)
//        }
//    }
//
//    @Test
//    fun readerWriter() {
//        val x = Boss.Writer()
//        x.write("foo")
//        x.write(null)
//        x.write("bar")
//        val r = Boss.Reader(x.toByteArray())
//        assertEquals(BossPlatform.Result.Ok("foo"), r.readResult())
//        assertEquals(BossPlatform.Result.Ok(null), r.readResult(), )
//        assertEquals(BossPlatform.Result.Ok("bar"), r.readResult())
//        assertEquals(BossPlatform.Result.EOF, r.readResult())
//        assertEquals(BossPlatform.Result.EOF, r.readResult())
//    }
//
//    @Test
//    fun simpleSerializerTest() {
//        val value = Foobar(42, "global")
//        val encoded = BossEncoder.encode(value)
//        println("------------------------------------------------")
//        println(encoded.toDump())
//        println("------------------------------------------------")
//        println("-- ${encoded.decodeBoss<BossStruct>()}")
//        println("-- ${encoded.decodeBoss<Foobar>()}")
//        assertEquals(value, encoded.decodeBoss<Foobar>())
//    }
}