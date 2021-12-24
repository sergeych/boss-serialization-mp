@file:Suppress("UNCHECKED_CAST")

package net.sergeych.bossk

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.datetime.Instant
import net.sergeych.boss_serialization_mp.BossStruct
import net.sergeych.mptools.*
import kotlin.math.abs

class AssertionFailedException(text: String) : Exception(text)

internal fun assert(condition: Boolean, text: String) {
    if (!condition) throw AssertionFailedException(text)
}

/**
 * Multiplatform Boss codec for Kotlin based on channels, coroutine friendly and could also be used to process
 * unlimited binary data streams (through in Channel) without framing. This version also support platform-dependent
 * data conversion to Boss primitives.
 *
 * Boss is a bit-effective binary packing format that uses optimized for size data format, having compact
 * representation for frequently used values and caching for repreating objects. It supports following types:
 *
 * - Integer, longs, arbitrary-length integers (BigIntegers), which are automatically packed to use only necessary
 *   number of bytes to store
 * - doubles (with effective encoding of 0 and ±1.0)
 * - strings (using UTF-8) with caching
 * - binary data, effectively packed (no deviations like base64)
 * - time Instants, rounded to second but otherwise unlimited (as long as it fits Instant data field)
 * - lists of such objects
 * - maps where both keys and values are any objects above (still we recommend to limit to key types that are
 *   supported everywhere, as, for example, javascript Map can't use arbitrary object as a key in a correct way.
 *
 * To convert some objects to and from binary representation, use [Bossk.pack], [Bossk.packWith] and [Boss.unpackWith]
 * and [Bossk.unpack] respectively.
 *
 * Bossk also support streaming mode over kotlin `Channel<Byte>`, see [Bossk.Reader] and [Bossk.Writer]
 */
object Bossk {

    /**
     * Interface to filter (and possibly convert) source objects. Could be used to automatically convert
     * user domain objects to boss-compatible data formats and vice versa.
     */
    interface Converter {
        /**
         * called _before boss serialization_, allowing to convert user field types to boiss supported, e.g.
         * ZonedDateTime on JVM could be autpconverted to multiplatofrm `kotlinx.datetime.Instant`, or even
         * perform complex serialization of objects to structures (hashes) with some type tag.
         */
        fun toBoss(source: Any?): Any?

        /**
         * Called _after boss serialization_ allowing to convert boss field types to user domain objects.
         * e.g. on JVM target it can convert `kotlinx.datetime.Instant` boss uses internally to, for example,
         * `ZonedDateTime`, or perform complex object deserialization from a cache.
         */
        fun fromBoss(packed: Any?): Any?
    }

    private const val TYPE_INT = 0
    private const val TYPE_EXTRA = 1
    private const val TYPE_NINT = 2
    private const val TYPE_TEXT = 3
    private const val TYPE_BIN = 4
    private const val TYPE_CREF = 5
    private const val TYPE_LIST = 6
    private const val TYPE_DICT = 7
    private const val XT_DZERO = 1 // double 0.0
    private const val XT_DONE = 2 // double 1.0
    private const val XT_DMINUSONE = 4 // double -1.0

    // TFLOAT = 6; // 32-bit IEEE float we do not use now
    private const val XT_DOUBLE = 7 // 64-bit IEEE float
    private const val XT_TTRUE = 12
    private const val XT_FALSE = 13

    // static private final int TCOMPRESSED = 14;
    private const val XT_TIME = 15
    private const val XT_STREAM_MODE = 16

    // These modes are not used as programming language (e.g. python) binfings are moved out from
    // the main BOSS specification in 2009, but are still available for extension"
    // static private final int TOBJECT = 8; // object record
    // TMETHOD = 9; // instance method
    // TFUNCTION = 10; // callable function
    // TGLOBREF = 11; // global reference

    suspend fun packWith(converter: Converter?, obj: Any?): ByteArray {
        return try {
            ByteArrayWriter(converter=converter).also { it.writeObject(obj) }.toByteArray()
        } catch (ex: Exception) {
            throw TypeException("Boss can't dump this object", ex)
        }
    }

    // works:
    // suspend fun pack(vararg obj: Any?): ByteArray = packWith(null, obj[0])
    // Fails!
    suspend fun pack(obj: Any?): ByteArray = packWith(null, obj)

    suspend fun <T> unpackWith(converter: Converter?,source: ByteArray): T = Reader(source.openChannel(),converter).read()

    suspend fun <T> unpack(source: ByteArray): T = unpackWith(null, source)


    private fun traceObject(prefix: String, obj: Any?): String = when (obj) {
        is ByteArray -> {
            if (obj.size > 30)
                prefix + obj.slice(0..29).toHex() + "...(${obj.size} bytes)"
            else
                "$prefix${obj.toHex()}"
        }
        is Array<*> -> traceList(prefix, obj.toList())
        is List<*> -> traceList(prefix, obj)
        is BossStruct -> obj.toString()
        null -> "null"
        else -> """"$obj""""
    }

    private fun traceList(prefix: String, objects: Collection<*>): String {
        val b = StringBuilder()
        for ((i, x) in objects.withIndex()) {
            val p1 = "$prefix $i: "
            b.append(traceObject(p1, x))
        }
        return b.toString()
    }

    internal class Header {
        var code: Int
        var value: Long = 0
        var bigValue: BigInteger? = null

        constructor(_code: Int, _value: Long) {
            code = _code
            value = _value
        }

        constructor(_code: Int, big: BigInteger?) {
            bigValue = big
            code = _code
        }

        fun smallestNumber(negative: Boolean): Any {
            bigValue?.let { return if (negative) -it else it }
            if (abs(value) <= Int.MAX_VALUE) return if (negative) -value.toInt() else value.toInt()
            return if (negative) -value else value
        }

        override fun toString(): String {
            return "BH: code=$code value=$value bigValue=$bigValue"
        }
    }

    /**
     * BOSS serializer. Serialized object trees or, in stream mode, could be used to seralize a stream of objects.
     *
     * @author sergeych
     */
    open class Writer(
        private val out: SendChannel<Byte>,
        private val converter: Converter? = null
    ) {
        //        private val out: java.io.OutputStream
        private var cache = HashMap<Any?, Int>()
        private var treeMode = true
//        private val biSerializer: BiSerializer?
        /**
         * Creates writer to write to the output stream. Upon creation writer is alwais in tree mode.
         *
         * @param outputStream See [.setStreamMode]
         */
        /**
         * Creates writer to write to the output stream. Upon creation writer is alwais in tree mode.
         *
         * @param outputStream See [.setStreamMode]
         */
        init {
            cache.put(null, 0)
        }

        /**
         * Turn encoder to stream mode (e.g. no cache). In stram mode the protocol do not never cache nor remember
         * references, so restored object tree will not correspond to sources as all shared nodes will be copied. Stream
         * mode is used in large streams to avoid unlimited cache growths.
         *
         *
         * Stream more pushes the special record to the stream so the decoder [Reader] will know the more. Before
         * entering stream mode it is theoretically possible to write some cached trees, but this feature is yet
         * untested.
         *
         * @throws IOException
         */
        suspend fun setStreamMode() {
            cache.clear()
            cache.put(null, 0)
            treeMode = false
            writeHeader(TYPE_EXTRA, XT_STREAM_MODE.toLong())
        }

        /**
         * Serialize one or more objects. Objects will be serialized one by one, so corresponding number of read()'s is
         * necessary to retrieve them all.
         *
         * @param objects any number of Objects known to BOSS
         *
         * @return this Writer instance
         *
         * @throws IOException
         */
        suspend fun write(vararg objects: Any): Writer {
            for (x in objects) put(x)
            return this
        }

        /**
         * Serialize single object known to boss (e.g. integers, strings, byte[] arrays or Bytes class instances, Date
         * instances, arrays, [ArrayList], [HashMap]
         *
         * @param obj the root object to encode
         *
         * @return this instance to allow chaining calls
         *
         * @throws IOException
         */
        suspend fun writeObject(obj: Any?): Writer {
//            if (biSerializer != null && !(obj is Number || obj is String || obj is java.time.ZonedDateTime
//                        || obj is Boolean)
//            ) put(biSerializer.serialize<Any>(obj)) else put(obj)
            put(obj)
            return this
        }

        private suspend fun send(value: Int) {
            out.send((value and 0xFF).toByte())
        }

        private suspend fun writeHeader(code: Int, value: BigInteger) {
            send(code or 0xF8)
            val bb = value.toByteArray()
            bb.flipSelf()
            writeEncoded(bb.size.toLong())
            out.sendAll(bb)
        }

        private suspend fun writeHeader(code: Int, _value: Long) {
            var value = _value
            assert(code >= 0 && code <= 7, "code is out of range 0..7: $code")
            assert(value >= 0, "value can't be negative: $value")
            if (value < 23) send(code or (value.toInt() shl 3)) else {
                var n: Int = sizeInBytes(value)
                if (n < 9) {
                    send(code or (n + 22 shl 3))
                } else {
                    send(code or 0xF8)
                    writeEncoded(n.toLong())
                }
                while (n-- > 0) {
                    send(value.toInt() and 0xFF)
                    value = value ushr 8
                }
            }
        }


        private suspend fun put(data: Any?) {
            val obj = converter?.let { it.toBoss(data)} ?: data
            when (obj) {
                is BigInteger -> {
                    if (obj.signum() >= 0) writeHeader(TYPE_INT, obj)
                    else writeHeader(TYPE_NINT, obj.negate())
                }
                is Number -> {
                    // n JS we can't test it for "is Long" or "is Int"
                    val d = obj.toDouble()
                    if ( (obj is Long || obj is Int) && d == obj.toLong().toDouble()) {
                        if (obj.toLong() >= 0)
                            writeHeader(TYPE_INT, obj.toLong())
                        else
                            writeHeader(TYPE_NINT, -obj.toLong())
                        return
                    }
                    // Should be double
                    when (d) {
                        0.0 -> writeHeader(TYPE_EXTRA, XT_DZERO.toLong())
                        -1.0 -> writeHeader(TYPE_EXTRA, XT_DMINUSONE.toLong())
                        1.0 -> writeHeader(TYPE_EXTRA, XT_DONE.toLong())
                        else -> {
                            writeHeader(TYPE_EXTRA, XT_DOUBLE.toLong())
                            out.sendAll(longToBytes(d.toBits()).flip())
                        }
                    }
                }
                is String -> writeString(obj.toString())
                is CharSequence -> writeString(obj.toString())
                is ByteArray -> {
                    if (!tryWriteReference(obj)) {
                        writeHeader(TYPE_BIN, obj.size.toLong())
                        out.sendAll(obj)
                    }
                }
                is Array<*> -> writeArray(obj)
                is Boolean -> writeHeader(TYPE_EXTRA, (if (obj) XT_TTRUE else XT_FALSE).toLong())
                is Instant -> {
                    writeHeader(TYPE_EXTRA, XT_TIME.toLong())
                    writeEncoded(obj.epochSeconds)
                }
                is Map<*, *> -> writeMap(obj)
                is Collection<*> -> writeArray(obj)
                null -> writeHeader(TYPE_CREF, 0)
                else -> throw TypeException("unknown type: ${obj::class.simpleName}")
            }
        }

        private suspend fun writeString(s: String) {
            if (!tryWriteReference(s)) {
                val bb = s.encodeToByteArray()
                writeHeader(TYPE_TEXT, bb.size.toLong())
                out.sendAll(bb)
            }
        }

        private suspend fun writeMap(obj: Any) {
            if (!tryWriteReference(obj)) {
                val map = obj as Map<*, *>
                writeHeader(TYPE_DICT, map.size.toLong())
                for ((key, value) in map) {
                    put(key)
                    put(value)
                }
            }
        }

        private suspend fun writeArray(array: Array<*>) {
            if (!tryWriteReference(array)) {
                writeHeader(TYPE_LIST, array.size.toLong())
                for (x in array) put(x)
            }
        }

        private suspend fun writeArray(collection: Collection<*>) {
            if (!tryWriteReference(collection)) {
                writeHeader(TYPE_LIST, collection.size.toLong())
                for (x in collection) put(x)
            }
        }

        private suspend fun tryWriteReference(obj: Any): Boolean = cache.get(obj)?.let { index ->
            writeHeader(TYPE_CREF, index.toLong())
            return true
        } ?: run {
            // Cache put depends on the streamMode
            if (treeMode) cache.put(obj, cache.size)
            false
        }

        private suspend fun writeEncoded(_value: Long) {
            var value = _value
            while (value > 0x7f) {
                send(value.toInt() and 0x7f)
                value = value shr 7
            }
            send(value.toInt() or 0x80)
        }

        fun close() {
            out.close()
        }

        companion object {
            private fun sizeInBytes(_value: Long): Int {
                var value = _value
                var cnt = 1
                while (value > 255) {
                    cnt++
                    value = value ushr 8
                }
                return cnt
            }
        }
    }

    class ByteArrayWriter(
        private val channel: ByteArrayOutputChannel = ByteArrayOutputChannel(),
        private val converter: Converter? = null
    ) : Writer(channel, converter) {
        suspend fun toByteArray(): ByteArray = channel.toByteArray()
    }

    class Reader(
        private val channel: ReceiveChannel<Byte>,
        private val converter: Converter? = null
    ) {
        protected var treeMode = true
        protected var showTrace = false
        private var cache = mutableListOf<Any>()
        private val maxCacheEntries = 0
        private val maxStringSize = 0

        suspend fun traceObject() {
            println(readHeader())
        }

        private suspend fun readHeader(): Header {
            val b = readByte()
            val code = b and 7
            val value = b ushr 3
            return when {
                value >= 31 -> {
                    val length = readEncodedLong().toInt()
                    Header(code, readBig(length))
                }
                value > 22 ->
                    // up to 8 bytes, e.g. long
                    Header(code, readLong(value - 22))
                else -> Header(code, value.toLong())
            }
        }

        /**
         * Read byte or throw EOFException
         *
         * @return 0..255 byte value
         */
        private suspend fun readByte(): Int = channel.receive().toUByte().toInt()

        private suspend fun readEncodedLong(): Long {
            var value: Long = 0
            var shift = 0
            while (true) {
                val n = readByte()
                value = value or (n.toLong() and 0x7F shl shift)
                if (n and 0x80 != 0) return value
                shift += 7
            }
        }

        private suspend fun readBig(length: Int): BigInteger {
            val bytes = channel.reciveBytes(length)
            bytes.flipSelf()
            return BigInteger.fromByteArray(bytes, Sign.POSITIVE)
        }

        private suspend fun readLong(length: Int): Long {
            var l = length
            return if (l <= 8) {
                var res: Long = 0
                var n = 0
                while (l-- > 0) {
                    res = res or (readByte().toLong() shl n)
                    n += 8
                }
                res
            } else throw FormatException("readlLong needs up to 8 bytes as length")
        }

        fun setTrace(on: Boolean) {
            showTrace = on
        }

        protected fun trace(s: String?) {
            if (showTrace) println(s)
        }

        private fun traceCache() {
            trace("Cache: " + cache.toString())
        }

        /**
         * Read next object from the stream
         *
         * @param <T> expected object type
         *
         * @return next object casted to (T)
        </T> */
        suspend fun <T> read(): T = get()
//            val x = get<Any>()!!
//                x as T
//            return if (deserializer == null || !(x is net.sergeych.tools.Binder || x is Collection<*>)) x as T else deserializer.deserialize<T, Any>(
//            )
//    }

        private suspend fun <T> get(): T {
            val h = readHeader()
            val result = when (h.code) {
                TYPE_INT -> //                    trace("Int: " + h.smallestNumber(false));
                    h.smallestNumber(false) as T
                TYPE_NINT -> return h.smallestNumber(true) as T
                TYPE_BIN, TYPE_TEXT -> {
                    val bb = if (h.value > 0) channel.reciveBytes(h.value.toInt()) else byteArrayOf()
                    if (h.code == TYPE_TEXT) {
                        val s = bb.decodeToString()
                        cacheObject(s)
                        s as T
                    } else {
                        cacheObject(bb)
                        bb as T
                    }
                }
                TYPE_LIST -> {
                    val data = ArrayList<Any?>((if (h.value < 0x10000) h.value else 0x10000).toInt())
                    cacheObject(data)
                    for (i in 0 until h.value)
                        data.add(get())
                    data as T
                }
                TYPE_DICT -> readObject<T>(h)
                TYPE_CREF -> {
                    val i: Int = h.value.toInt()
                    (if (i == 0) null else cache[i - 1]) as T
                }
                TYPE_EXTRA -> parseExtra(h.value.toInt()) as T
                else -> throw FormatException("Bad BOSS header")
            }
            return converter?.let { it.fromBoss(result) as T } ?: result
        }

        private suspend fun <T> readObject(h: Header): T {
            val dict = HashMap<Any, Any?>()
            cacheObject(dict)
            for (i in 0 until h.value) dict.put(get(), get())
            //            if( hash.containsKey("__type") || hash.containsKey("__t"))
//                return (T) deserializer.deserialize(hash);
//            trace("Dict: " + hash);
            return dict as T
        }

        suspend fun readInt(): Int = (get() as Number).toInt()

        private fun cacheObject(obj: Any) {
            if (treeMode) cache.add(obj) else {

                // right now this is disabled (maxStringSize is 0)
                // this logic is buggy and needs to be redesigned and reimplemented

//                val len: Long // = 0
//                len = if (obj is String) {
//                    obj.length.toLong()
//                } else if (obj is ByteArray) {
//                    obj.size.toLong()
//                } else {
//                    // can't cache it
//                    return
//                }
//                if (len <= maxStringSize) {
//                    cache.add(obj)
//                    if (cache.size > maxCacheEntries) cache.removeAt(0)
//                }
            }
        }

        private suspend fun parseExtra(code: Int): Any {
            return when (code) {
                XT_DZERO -> 0.0
                XT_DONE -> 1.0
                XT_DMINUSONE -> -1.0
                XT_TTRUE -> true
                XT_FALSE -> false
                XT_TIME -> Instant.fromEpochSeconds(readEncodedLong())
                XT_STREAM_MODE -> {
                    setStreamMode()
                    // and ignore second parameter:
                    get<Any>()
                }
                XT_DOUBLE -> {
                    val data = channel.reciveBytes(8)
                    Double.fromBits(bytesToLong(data))
                }
                else -> throw FormatException("Unknown extra code: ${code}")
            }
        }

        private fun setStreamMode() {
            if (cache.size > 0) cache.clear()
            treeMode = false
        }
    }
}