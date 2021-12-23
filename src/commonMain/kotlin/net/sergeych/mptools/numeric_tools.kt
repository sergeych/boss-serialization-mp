package net.sergeych.mptools

import net.sergeych.bossk.FormatException
import net.sergeych.bossk.TypeException
import kotlin.math.min


/**
 * Convert long to LE bytes
 */
fun longToBytes(value: Long): ByteArray {
    var l = value
    val result = ByteArray(8)
    for (i in 7 downTo 0) {
        result[i] = (l and 0xFF).toByte()
        l = l shr 8
    }
    return result
}

/**
 * Convert 8 bytes to LE long
 */
fun bytesToLong(b: ByteArray): Long {
    var result: Long = 0
    for (i in 0 until 8) {
        result = result shl 8
        result = result or (b[i].toLong() and 0xFF)
    }
    return result
}

private val hexDigits = "0123456789ABCDEF"

fun Long.toHex(length: Int = 0): String {
    var result = ""
    var value = this
    if (value < 0) throw TypeException("cant convert to hex negative (ambiguous)")
    do {
        result = hexDigits[(value and 0x0f).toInt()] + result
        value = value shr 4
    } while (value > 0)
    while (result.length < length) result = "0" + result
    return result
}

fun Int.toHex(length: Int = 0) = toLong().toHex(length)
fun UInt.toHex(length: Int = 0) = toLong().toHex(length)
fun Byte.toHex(length: Int = 0) = toLong().toHex(length)
fun UByte.toHex(length: Int = 0) = toLong().toHex(length)
fun ULong.toHex(length: Int = 0) = toLong().toHex(length)

fun ByteArray.toHex(separator: String = " "): String = joinToString(separator) { it.toUByte().toHex(2) }
fun Collection<Byte>.toHex(separator: String = " "): String = joinToString(separator) { it.toUByte().toHex(2) }

fun ByteArray.toDump(wide: Boolean = false): String = toDumpLines(wide).joinToString("\n")

fun ByteArray.toDumpLines(wide: Boolean = false): List<String> {

    val lineSize = if (wide) 32 else 16

    fun dumpChars(_from: Int): String {
        var from = _from
        val b = StringBuilder(22)

        b.append('|')
        val max: Int = min(size, from + lineSize)
        while (from < max) {
            val ch = this[from++].toInt()
            if (ch >= ' '.code && ch < 127) b.append(ch.toChar()) else b.append('.')
        }
        val remainder = from % lineSize
        if (remainder > 0) {
            var cnt = lineSize - remainder
            while (cnt-- > 0) b.append(' ')
        }
        return b.append("|").toString()
    }


    val lines = mutableListOf<String>()
    if (size == 0) return lines
    var line: StringBuilder? = null

    if (size != 0) {
        for (i in indices) {
            if (i % lineSize == 0) {
                if (line != null) {
                    line.append(dumpChars(i - lineSize))
                    lines.add(line.toString())
                }
                line = StringBuilder(i.toHex(4))
                line.append(' ')
            }
            line!!.append((this[i].toUByte()).toHex(2))
            line.append(' ')
        }
        if (line != null) {
            val l = size
            var fill = lineSize - l % lineSize
            if (fill < lineSize) while (fill-- > 0) line.append("   ")
            val index = l - l % lineSize
            line.append(dumpChars(if (index < l) index else l - lineSize))
            lines.add(line.toString())
        }
    }
    return lines
}

fun String.decodeHex(): ByteArray {
    val source = this.trim().uppercase()
    val result = arrayListOf<Byte>()
    var pos = 0
    while (pos < source.length) {
        val i = hexDigits.indexOf(source[pos++])
        if (i < 0) throw FormatException("invalid hex digit in ${source} at ${pos - 1}")
        if (pos >= source.length) throw FormatException(
            "hex string must consist of bytes " +
                    "(unexepced end of data): $source"
        )
        val j = hexDigits.indexOf(source[pos++])
        if (j < 0) throw FormatException("invalid hex digit in ${source} at ${pos - 1}")
        result.add(((i shl 4) or j).toByte())
        while (pos < source.length && isSpace(source[pos])) pos++
    }
    return result.toByteArray()
}

fun ByteArray.flipSelf() {
    var i = 0
    var j = size - 1
    while (i < j) {
        val x = this[i]
        this[i++] = this[j]
        this[j--] = x
    }
}

fun ByteArray.flip(): ByteArray = copyOf().also { it.flipSelf() }
