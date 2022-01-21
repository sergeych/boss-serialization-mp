package net.sergeych.bossk

import net.sergeych.mptools.decodeBase64
import net.sergeych.mptools.encodeToBase64
import java.util.*
import kotlin.random.Random
import kotlin.test.*

class TestBase64 {

    @Test
    fun testBase64() {
        for( i in 0..10) {
            for (s in 1..117) {
                val src = Random.Default.nextBytes(s)
                val x = src.encodeToBase64()
                assertEquals(Base64.getEncoder().encode(src).decodeToString(), x)
                assertContentEquals(src, x.decodeBase64())
            }
            println("round $i")
        }
    }
}