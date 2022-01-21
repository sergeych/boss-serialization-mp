package net.sergeych.mptools

fun isSpace(char: Char): Boolean = when(char) {
    ' ', '\n', '\r', '\t' -> true
    else -> false
}

//private val reSpaces = Regex("\\s+")
//
