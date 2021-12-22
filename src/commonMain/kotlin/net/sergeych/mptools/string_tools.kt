package net.sergeych.mptools

import net.sergeych.platform.TypeException
import kotlin.math.min

private val hexDigits = "0123456789abcdef"

fun Long.toHex(length: Int = 0): String {
    var result = ""
    var value = this
    if( value < 0 ) throw TypeException("cant convert to hex negative (ambiguous)")
    do {
        result = hexDigits[(value and 0x0f).toInt()] + result
        value = value shr 4
    }  while(value > 0)
    while(result.length < length) result = "0" + result
    return result
}

fun Int.toHex(length: Int=0) = toLong().toHex(length)
fun UInt.toHex(length: Int=0) = toLong().toHex(length)
fun Byte.toHex(length: Int=0) = toLong().toHex(length)
fun UByte.toHex(length: Int=0) = toLong().toHex(length)
fun ULong.toHex(length: Int=0) = toLong().toHex(length)

fun ByteArray.toDump(wide: Boolean=false): String = toDumpLines(wide).joinToString("\n")

fun ByteArray.toDumpLines(wide:Boolean = false): List<String> {

    val lineSize = if( wide ) 32 else 16

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
    if( size == 0 ) return lines
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
            line!!.append(' ')
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
