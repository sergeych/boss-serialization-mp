@file:Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")

package net.sergeych.platform

import kotlinx.browser.localStorage
import kotlinx.browser.sessionStorage
import net.sergeych.boss_serialization_mp.*
import kotlin.test.*

class TestLocalStorage {

    fun DefaultKVStorage(name: String) = KVStorage(BrowserBinaryStorage(sessionStorage,name))

    @Test
    fun testNamedLocalStorage() {
        localStorage.clear()
        val ds = DefaultKVStorage("test_1")
        var x: String by kvStorage(ds)
        var y: Int by kvStorage(ds)
        x = "foo"
        y = 42
        val ds1 = DefaultKVStorage("test_2_long")
        var x1: String by kvStorage(ds1)
        var y1: Int by kvStorage(ds1, "y")
        x1 = "bar"
        y1 = 142
        println(ds.keys)
        println(ds1.keys)
        assertEquals( setOf("x", "y"), BrowserBinaryStorage(sessionStorage,"test_1").keys)
        assertEquals( setOf("x1", "y"), BrowserBinaryStorage(sessionStorage,"test_2_long").keys)

        var r = DefaultKVStorage("test_1")
        assertEquals("foo", r["x"])
        assertEquals(42, r["y"])

        r = DefaultKVStorage("test_2_long")
        assertEquals("bar", r["x1"])
        assertEquals(142, r["y"])
        r.clear()

        r = DefaultKVStorage("test_2_long")
        assertNull(r["x1"])
        assertNull(r["y"])

        r = DefaultKVStorage("test_1")
        assertEquals("foo", r["x"])
        assertEquals(42, r["y"])

//        var r = De
//        println(DefaultBinaryStorage("test_2_long").keys)
    }
}