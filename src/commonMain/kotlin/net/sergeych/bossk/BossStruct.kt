package net.sergeych.boss_serialization_mp

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.sergeych.mptools.toHex
import kotlin.collections.HashMap

/**
 * Wrap for Map<String,Any?> to allow de/serialization of the Maps with arbitrary content. Due to some
 * architectural limitations of `kotlinx.serialization` module, this is the only way to include such
 * map fields in the objects to be de/serialized, otherwise it won't be processed properly. It also has
 * few convenience methods to access typed data.
 */
@Serializable
@Suppress("UNCHECKED_CAST")
class BossStruct(private val __source: MutableMap<String, @Contextual Any?> = HashMap()) :
    MutableMap<String, Any?> by __source {
    /**
     * Get the element as a BossStruct, creating if necessary, as wrap around any other Map that
     * presents. E.g. if you need a BossStruct, whatever data is held here (e.g. Boss.Dictionary, is most often
     * used while decoding boss binaries), use this method.
     */
    @Suppress("unused")
    fun getStruct(key: String): BossStruct? = get(key)?.let {
        if (it is BossStruct) it else BossStruct(it as MutableMap<String, Any?>)
    }

    /**
     * Get and cast to a given type. Utility method.
     */
    fun <T> getAs(key: String): T = get(key) as T

    fun getByteArray(key: String): ByteArray? = getAs(key)

    override fun toString(): String =
        "{" + this.__source.entries.joinToString(", ") { (k,v) ->
            "$k=${formatItem(v)}"
        } + "}"
//    override fun toString(): String = __source.toString()

    @Suppress("unused")
    companion object {
        /**
         * Inherently empty instance of the BossStruct which can not be modified in any way.
         * Could be used, for example, to check return values.
         */
        val EMPTY by lazy { BossStruct(HashMap()) }

        /**
         * Try to get a [BossStruct] out of an argument, using the following strategy:
         *
         * - if it is an instance of a [BossStruct], returns it
         * - if it is a `MutableMap`, creates a [BossStruct] instance over it.
         * - if it is a `Map`, creates a [BossStruct] instance with a mutable copy of it
         * - otherwise, throw `IllegalArgumentException`.
         *
         * @return BossStruct instance as described above
         * @throws ClassCastException if conversion is impossible
         */
        fun from(source: Any): BossStruct =
            when (source) {
                is BossStruct -> source
                is MutableMap<*, *> -> BossStruct(source as MutableMap<String, Any?>)
                is Map<*, *> -> BossStruct((source as Map<String, Any?>).toMutableMap())
                else -> throw ClassCastException("can't convert to BossStruct: $source")
            }

        fun from(vararg data: Pair<String, Any?>): BossStruct = BossStruct().also {
            for ((key, value) in data)
                it[key] = value
        }

        private fun formatItem(item: Any?): String =
            when(item) {
                null -> "null"
                is BossStruct -> item.toString()
                is Map<*,*> -> "{${from(item)}"
                is List<*> -> {
                    "[${item.joinToString(",") { formatItem(it) }}]"
                }
                is Array<*> -> {
                    "[${item.joinToString(",") { formatItem(it) }}>"
                }
                is ByteArray -> formatBinary(item)
                else -> item.toString()
            }

        private fun formatBinary(item: ByteArray): String {
            val start = item.take(7).joinToString(" ") { it.toHex(2) }
            return if (item.size <= 7)
                "|$start|"
            else
                "|$start…(${item.size})|"
        }
    }
}

fun bossStructOf(vararg pairs: Pair<String,Any?>) = BossStruct.from(*pairs)