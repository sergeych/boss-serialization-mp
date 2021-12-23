import kotlinx.coroutines.runBlocking
import net.sergeych.mptools.ByteArrayOutputChannel
import net.sergeych.mptools.openChannel
import net.sergeych.mptools.toHex
import kotlin.test.Test
import kotlin.test.assertContentEquals

internal class ToolsTestNative {
    @Test
    fun toFlow() {
        runBlocking {
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