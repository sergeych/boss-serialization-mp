@file:Suppress("UNUSED_VALUE")

package boss_serialization_mp

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import net.sergeych.boss_serialization_mp.*
import net.sergeych.mptools.truncateToSeconds
import kotlin.test.*


class StorageTest {

    @Serializable
    data class TX(val foo: String, val bar: Int)

//    class AA(storage: KVStorage) {
//        val hello: String by kvStorage(storage)
//    }

    @Test
    fun testStorage() {
        val s = KVStorage(MemoryKVBinaryStorage())
        assertNull(s["foo"])
        s["foo"] = "bar"
        assertEquals("bar", s.get<String>("foo"))
        assertEquals("bar", s["foo"])
        s["42"] = 42
        assertEquals(42, s["42"])
        s["43"] = true
        assertEquals(true, s["43"])
        // this does not produce exception on JS, sadly
//        assertFailsWith<ClassCastException> { println("" + (121 == s.get<Int>("43"))) }

        val x = Clock.System.now().truncateToSeconds()
        s["foo"] = x
        assertEquals(x, s["foo"])

        val d = "Helluva".encodeToByteArray()
        s["hello"] = d
        assertContentEquals(d, s["hello"])

        val tx = TX("sense", 42)
        s["ser1"] = tx
        assertEquals(tx, s.get<TX>("ser1"))

        s["some"] = "value-2"
        var some: String by kvStorage(s)
        assertEquals("value-2", some)
        some = "42"
        assertEquals("42", s["some"])

        var ser1: TX? by optKvStorage(s)
        assertEquals(tx, ser1)

        val tx2 = TX("bar", 111)
        ser1 = tx2
        assertEquals(tx2, ser1)

        s["nulx"] = tx2
        val nulx by optKvStorage<TX>(s)
        assertEquals(tx2, nulx)
        s["nulx"] = ser1
        assertEquals(ser1, nulx)
        s.delete("nulx")
        assertNull(nulx)
//        println("SUCCESS")

        val n2 by optKvStorage<TX>(s, "over")
        assertNull(n2)
        s["over"] = tx2
        assertEquals(tx2, n2!!)
    }

}