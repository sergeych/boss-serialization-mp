package net.sergeych.boss_serialization_mp

import net.sergeych.mptools.decodeBase64Compact
import net.sergeych.mptools.encodeToBase64Compact
import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * Prefix-key based binary storage backed by a `dom.Storage instance`, e.g. will work with localStorage and
 * sessionStorage fairly well. The key spaces are separated by adding a prefix derived fro a name, so storages
 * are to some extent independent. Clearing such storage should not affect other storages sharing the same
 * browser `dom.Storage`.
 */
class BrowserBinaryStorage(val storage: Storage, name: String = "") : KVBinaryStorage {

    private val usePrefix = name != ""
    private val preix = "_:_$name:"

    fun n(key: String) = if (usePrefix) preix + key else key

    override fun get(key: String): ByteArray? = storage.get(n(key))?.decodeBase64Compact()

    override fun set(key: String, value: ByteArray) {
        storage.set(n(key), value.encodeToBase64Compact())
    }

    override fun remove(key: String): ByteArray? {
        val k = n(key)
        return get(k).also { storage.removeItem(k) }
    }

    override val keys: Set<String>
        get() = mutableSetOf<String>().also { result ->
            for (i in 0 until storage.length)
                storage.key(i)?.let {
                    if (!usePrefix || it.startsWith(preix)) {
                        println("added prefixed: $it")
                        result.add(it.substring(preix.length))
                    }
                }
        }
}

