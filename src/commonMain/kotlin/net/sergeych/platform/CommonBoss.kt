package net.sergeych.platform

import net.sergeych.boss_serialization_mp.BossStruct

/**
 * Boss codec has different implementations (as for now), this is the common interface
 * to it, to be used instead of platform implementations:
 */
interface BossPlatform {

    sealed class Result {

        data class Ok<T:Any?>(val value: T): Result()

        object EOF: Result() {
            override fun toString(): String {
                return "BossPlatform.Result.EOF"
            }
        }
    }

    interface Input {
        fun readResult(): Result
    }

    interface Output {

        fun write(payload: Any?): Output

        fun toByteArray(): ByteArray
    }

    fun pack(payload: Any?): ByteArray

    fun <T: Any?>unpack(packed: ByteArray): T

    fun Reader(source: ByteArray): Input

    fun Writer(): Output
}

expect val Boss: BossPlatform


fun <T: Any?>BossPlatform.Input.read() =
    readResult().let {
        when(it) {
            is BossPlatform.Result.Ok<*> -> it.value as T
            BossPlatform.Result.EOF -> throw EOFException()
        }
    }

fun BossPlatform.Input.readStruct(): BossStruct {
    val x = read<Any?>()
    return when(x) {
        is BossStruct -> x
        is Map<*, *> -> BossStruct.from(x)
        null -> throw TypeException("can't convert null to BossStruct")
        else -> throw TypeException("can't convert to BossStruct: ${x::class.simpleName}")
    }
}

