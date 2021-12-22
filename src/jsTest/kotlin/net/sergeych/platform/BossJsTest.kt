package net.sergeych.platform

import net.sergeych.boss_serialization_mp.BossEncoder
import net.sergeych.boss_serialization_mp.BossStruct
import net.sergeych.boss_serialization_mp.decodeBoss
import net.sergeych.mptools.toDump
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame


@JsModule("dayjs")
@JsNonModule
external fun dayjs(): Any

internal class BossJsTest {

    @Test
    fun testBossBindings() {
        val x = "Oops"
        val y = Unicrypto.Boss.load(Unicrypto.Boss.dump(x))
        assertEquals(x, y)

        val w = Unicrypto.Boss.createWriter()
        w.write("foo")
        w.write("bar")
        val r = Unicrypto.Boss.createReader(w.get())
        assertEquals("foo", r.read())
        assertEquals("bar", r.read())
        assertSame(undefined, r.read())
        assertSame(undefined, r.read())
    }

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
    fun readerWriter2() {
        val x = Boss.Writer()
        val s = mapOf("bar" to "global", "foo" to 42)
        x.write(s)
//        x.write(null)
//        x.write(s)
        val r = Boss.Reader(x.toByteArray())
        println(">> ${r.readStruct()}")
//        println(">> ${r.readStruct()}")
//        println(">> ${r.readStruct()}")
//        println(">> ${r.readStruct()}")
//        assertEquals(BossPlatform.Result.Ok("foo"), r.readResult())
//        assertEquals(BossPlatform.Result.Ok(null), r.readResult(), )
//        assertEquals(BossPlatform.Result.Ok("bar"), r.readResult())
//        assertEquals(BossPlatform.Result.EOF, r.readResult())
//        assertEquals(BossPlatform.Result.EOF, r.readResult())
    }



    @Test
    fun simpleSerializerTest() {
        val value = Foobar(42, "global")
        println("@@ $value")
        val encoded = BossEncoder.encode(value)
        println("------------------------------------------------")
        println(encoded.toDump())
        println("------------------------------------------------")
//        println("-- ${encoded.decodeBoss<BossStruct>()}")
//        println("-- ${encoded.decodeBoss<Foobar>()}")
//        assertEquals(value, encoded.decodeBoss<Foobar>())
    }

}