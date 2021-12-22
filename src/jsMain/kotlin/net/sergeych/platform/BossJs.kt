package net.sergeych.platform

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

object BossJs : BossPlatform {

    override fun pack(payload: Any?): ByteArray = Unicrypto.Boss.dump(payload)

    override fun <T> unpack(packed: ByteArray): T = Unicrypto.Boss.load(packed) as T

    class Input(packed: ByteArray) : BossPlatform.Input {

        private val reader = Unicrypto.Boss.createReader(packed)

        override fun readResult(): BossPlatform.Result {
            val r = reader.read()
            if( r === undefined ) return BossPlatform.Result.EOF
            return BossPlatform.Result.Ok(r)
        }
    }

    class Output : BossPlatform.Output {

        private val writer = Unicrypto.Boss.createWriter()

        override fun write(payload: Any?): BossPlatform.Output {
            println("!! ee write $payload")
            writer.write(payload)
            return this
        }

        override fun toByteArray(): ByteArray = writer.get()
    }

    override fun Reader(source: ByteArray): BossPlatform.Input  = Input(source)

    override fun Writer(): BossPlatform.Output = Output()
}

actual val Boss: BossPlatform = BossJs

