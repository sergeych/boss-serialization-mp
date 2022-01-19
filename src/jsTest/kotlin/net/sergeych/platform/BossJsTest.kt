package net.sergeych.platform

import net.sergeych.bossk.Bossk
import org.khronos.webgl.Uint8Array
import kotlin.test.Test
import kotlin.test.assertContentEquals

class BossJsTest {
    @Test
    fun testWriterArray() {
        println("we start")
        return runTest {
//            println("\n\n -------------------------------------------- \n\n\n")
            val w = Bossk.ByteArrayWriter()
            w.write(byteArrayOf(1, 2, 3))
            w.write(Uint8Array(arrayOf(4, 5, 6)))
//            println(w.toByteArray().toHex())
            assertContentEquals(byteArrayOf(28, 1, 2, 3, 28, 4, 5, 6), w.toByteArray())
//            println("\n\n -------------------------------------------- \n\n\n")
        }
    }
}