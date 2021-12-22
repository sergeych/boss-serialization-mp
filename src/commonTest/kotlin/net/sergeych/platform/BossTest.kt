package net.sergeych.platform

import kotlinx.serialization.Serializable
import net.sergeych.boss_serialization_mp.BossEncoder
import net.sergeych.boss_serialization_mp.BossStruct
import net.sergeych.boss_serialization_mp.decodeBoss
import net.sergeych.mptools.toDump
import net.sergeych.mptools.toDumpLines
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
data class Foobar(val foo: Int,val bar: String)

internal class CommonBossTest {

    @Test
    fun packUnpack() {
        val x = Boss.pack("Hello")
        assertEquals("Hello", Boss.unpack(x))
    }

    @Test
    fun readerWriter() {
        val x = Boss.Writer()
        x.write("foo")
        x.write(null)
        x.write("bar")
        val r = Boss.Reader(x.toByteArray())
        assertEquals(BossPlatform.Result.Ok("foo"), r.readResult())
        assertEquals(BossPlatform.Result.Ok(null), r.readResult(), )
        assertEquals(BossPlatform.Result.Ok("bar"), r.readResult())
        assertEquals(BossPlatform.Result.EOF, r.readResult())
        assertEquals(BossPlatform.Result.EOF, r.readResult())
    }

    @Test
    fun simpleSerializerTest() {
        val value = Foobar(42, "global")
        val encoded = BossEncoder.encode(value)
        println("------------------------------------------------")
        println(encoded.toDump())
        println("------------------------------------------------")
        println("-- ${encoded.decodeBoss<BossStruct>()}")
        println("-- ${encoded.decodeBoss<Foobar>()}")
        assertEquals(value, encoded.decodeBoss<Foobar>())
    }
}