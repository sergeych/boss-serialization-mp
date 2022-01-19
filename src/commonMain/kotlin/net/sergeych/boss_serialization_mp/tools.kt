@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package net.sergeych.boss_serialization_mp

import kotlinx.serialization.ExperimentalSerializationApi
import net.sergeych.boss_serialization.BossDecoder
import net.sergeych.bossk.Bossk


/**
 * Decode boss object from this binary data into a given class instance
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> ByteArray.decodeBoss(): T = BossDecoder.decodeFrom(this)

/**
 * read and deserialize object from boss reader
 */
@Suppress("unused")
@OptIn(ExperimentalSerializationApi::class)
inline suspend fun <reified T> Bossk.Reader.deserialize(): T = BossDecoder.decodeFrom(this)!!

@Suppress("unused")
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> Bossk.SyncReader.deserialize(): T = BossDecoder.decodeFrom(this)!!

/**
 * ASCII dump representation for a binary data, with address, hex and ascii fields, following the
 * old tradition
 */
//fun ByteArray.dump(): String =
//    Bytes(this).toDump()
//
