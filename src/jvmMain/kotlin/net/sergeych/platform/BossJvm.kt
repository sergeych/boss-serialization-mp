@file:Suppress("UNCHECKED_CAST")

package net.sergeych.platform

import net.sergeych.boss.Boss
import net.sergeych.utils.Bytes
import java.io.ByteArrayOutputStream
import java.io.EOFException

internal fun <T:Any?>unpackBytes(result: Any?) = (if( result is Bytes ) result.toArray() else result) as T

object BossJvm : BossPlatform {

    private class BossReader(private val reader: Boss.Reader) : BossPlatform.Input {

        override fun readResult(): BossPlatform.Result =
            try {
                BossPlatform.Result.Ok(unpackBytes<Any?>( reader.read<Any?>()))
            } catch (x: EOFException) {
                BossPlatform.Result.EOF
            }
    }

    private class BossWriter: BossPlatform.Output {

        private val output = ByteArrayOutputStream()
        private val writer = net.sergeych.boss.Boss.Writer(output)

        override fun write(payload: Any?): BossPlatform.Output {
            writer.write(payload)
            return this
        }

        override fun toByteArray(): ByteArray = output.toByteArray()
    }

    override fun pack(payload: Any?): ByteArray =
        net.sergeych.boss.Boss.pack(payload)

    override fun <T> unpack(packed: ByteArray): T =
        unpackBytes(net.sergeych.boss.Boss.load<Any?>(packed))

    override fun Reader(source: ByteArray): BossPlatform.Input = BossReader(net.sergeych.boss.Boss.Reader(source))

    override fun Writer(): BossPlatform.Output = BossWriter()

}

actual val Boss: BossPlatform = BossJvm