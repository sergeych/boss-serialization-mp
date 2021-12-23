@file:OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)

package net.sergeych.mptools


import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel


/**
 * Get a channel that produces this array bytes that works in JS too. Async
 * variant if BytearrayInputStream, not available on js platform.
 */
@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
suspend fun ByteArray.openChannel(): ReceiveChannel<Byte> = Channel<Byte>(1).also { channel ->
    GlobalScope.launch {
        for (b in this@openChannel) {
            if (channel.isClosedForReceive) break
            channel.send(b)
        }
        channel.close()
    }
}

/**
 * Async analog of ByteArrayOutputStream, not available on JS platform. Important! [toByteArray] closes the channel
 * so it is not possible to get "partial" result from it.
 *
 */
class ByteArrayOutputChannel(bufferSize: Int = 256,
                             private val channel: Channel<Byte> = Channel<Byte>(bufferSize))
    : SendChannel<Byte> by (channel) {

    private val buffer = ArrayList<Byte>()

    /**
     * Close the channel, and return the collected bytes. It is not possible to [send] to this channel after
     * the call. It is safe to call this method more than one time.
     */
    suspend fun toByteArray(): ByteArray {
        if (!isClosedForSend) close()
        receiver.join()
        return buffer.toByteArray()
    }

    private val receiver = GlobalScope.launch {
        while (!channel.isClosedForReceive) {
            channel.receiveCatching().getOrNull()?.let { buffer.add(it) }
        }
    }
}

suspend fun SendChannel<Byte>.sendAll(data: ByteArray): SendChannel<Byte> {
    for( b in data) send(b)
    return this
}

suspend fun ReceiveChannel<Byte>.reciveBytes(length: Int): ByteArray {
    val result = ByteArray(length)
    for( i in 0 until length) result[i] = receive()
    return result
}

//@OptIn(ExperimentalUnsignedTypes::class)
//suspend fun UByteArray.asFlow(): Flow<UByte> = flow {
//    for( i in 0 until size ) emit(get(i))
//}
//
//suspend fun ByteArray.asChannel() {
//
//}