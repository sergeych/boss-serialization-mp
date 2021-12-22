package net.sergeych.platform

class EOFException : Exception("access past end of file")

class TypeException(reason: String = "invalid type"): Exception(reason)