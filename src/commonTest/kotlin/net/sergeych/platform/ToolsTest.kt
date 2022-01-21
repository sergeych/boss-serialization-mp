package net.sergeych.platform

import net.sergeych.mptools.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

private const val l = 0xf1a2b374e02

internal class ToolsTest {

    @Test
    fun formatHex() {
        assertEquals("11", 17.encodeToHex())
        assertEquals("011", 17.encodeToHex(3))
        assertEquals("F1A2B374E02", 0xf1a2b374e02.encodeToHex())
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
    fun toChannel() {
        return runTest {
            val source = byteArrayOf(5, 4, 3, 2, 1, 2, 3, 4, 5)
            val x = source.openChannel()
            val y = ByteArrayOutputChannel(5)
            for( a in x ) { y.send(a) }
            assertContentEquals(source, y.toByteArray())

        }
    }

    @Test
    fun testBase64() {
        for( i in 0..10) {
            for (s in 1..117) {
                val src = Random.Default.nextBytes(s)
                val x = src.encodeToBase64()
                assertContentEquals(src, x.decodeBase64())
            }
        }
    }

    @Test
    fun arrayToHex() {
        val source = byteArrayOf(1,2, -2, -1)
        assertEquals("01 02 FE FF", source.encodeToHex())
    }

}