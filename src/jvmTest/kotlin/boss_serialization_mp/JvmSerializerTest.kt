@file:UseSerializers(ZonedDateTimeSerializer::class)
@file:OptIn(ExperimentalSerializationApi::class)

package boss_serialization_mp

import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.sergeych.boss_serialization.BossDecoder
import net.sergeych.boss_serialization_mp.BossEncoder
import net.sergeych.boss_serialization_mp.ZonedDateTimeSerializer
import net.sergeych.platform.runTest
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class JvmSerializerTest {

//    @Serializable
//    data class FB(val foo: String,val bar: Int)

    @Serializable
    data class FBZ(val foo: String,val bar: Int,val dt: ZonedDateTime)

    @Test
    fun zonedDateTimeSupport() {
        return runTest {
            val x = FBZ("bazz", 42, ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS))
            val packed = BossEncoder.encodeToStruct(x)
//            println(packed)
            val dt = packed["dt"]!!
            assertIs<Instant>(dt)
//            println(BossEncoder.encode(x).toDump())
            val y = BossDecoder.decodeFrom<FBZ>(packed)
//            println(y)
            assertEquals(x, y)
            assertEquals(x, BossDecoder.decodeFrom<FBZ>(BossEncoder.encode(x)))
        }
    }
}