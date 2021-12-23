package net.sergeych.bossk

/*
To get MPP cpmatibility we shouldn't use JVM exceptions tree:
 */

class TypeException(reason: String = "invalid type",cause: Throwable? = null): Exception(reason,cause)

class FormatException(reason: String = "invalid data format",cause: Throwable? = null): Exception(reason,cause)