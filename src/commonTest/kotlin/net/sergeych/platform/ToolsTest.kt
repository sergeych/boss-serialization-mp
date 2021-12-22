package net.sergeych.platform

import net.sergeych.mptools.toHex
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ToolsTest {

    @Test
    fun formatHex() {
        assertEquals("11", 17.toHex())
        assertEquals("011", 17.toHex(3))
        assertEquals("f1a2b374e02", 0xf1a2b374e02.toHex())
    }

}