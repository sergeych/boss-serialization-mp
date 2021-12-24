package net.sergeych.platform

// USe it as a sample how to write unicrypto bindings
@JsModule("unicrypto")
@JsNonModule
external class Unicrypto {
    companion object {
        fun randomBytes(size: Int): Any
    }

    class Boss {
        companion object {
            fun dump(payload: Any?): ByteArray
            fun load(packed: ByteArray): Any?

            fun createWriter(): BossWriter
            fun createReader(packed: ByteArray): BossReader
        }
    }
}

external interface BossWriter {
    fun write(payload: Any?)
    fun get(): ByteArray
}

external interface BossReader {
    fun read(): dynamic
}
