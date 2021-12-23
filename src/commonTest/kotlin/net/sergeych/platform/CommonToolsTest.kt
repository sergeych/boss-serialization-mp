package net.sergeych.platform

import net.sergeych.mptools.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

private const val l = 0xf1a2b374e02

internal class CommonToolsTest {

    @Test
    fun formatHex() {
        assertEquals("11", 17.toHex())
        assertEquals("011", 17.toHex(3))
        assertEquals("F1A2B374E02", 0xf1a2b374e02.toHex())
    }

    @Test
    fun parseHex() {
        assertContentEquals(byteArrayOf(0x11, 0xE2.toByte()), "11 E2".decodeHex())
    }

    @Test
    fun bytesToLong() {
        val src = 34276945832498123L
        assertEquals(src, bytesToLong(longToBytes(src)))
    }

    @Test
    fun toFlow() {
        return runTest {
            val source = byteArrayOf(5, 4, 3, 2, 1, 2, 3, 4, 5)
            val x = source.openChannel()
            val y = ByteArrayOutputChannel(5)
            println("------------------------------------")
            for( a in x ) { println(a); y.send(a) }
            println(y.toByteArray())
            assertContentEquals(source, y.toByteArray())

        }
    }

    @Test
    fun arrayToHex() {
        val source = byteArrayOf(1,2, -2, -1)
        println(source.toHex())
    }

}