@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED", "UNCHECKED_CAST")

package net.sergeych.boss_serialization

import kotlinx.datetime.Instant
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.internal.NamedValueDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import net.sergeych.boss_serialization_mp.BossStruct
import net.sergeych.bossk.Bossk
import net.sergeych.mptools.openChannel
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface SpecificConverter {
    val descriptor: SerialDescriptor
    fun serialize(source: Any): Any
    fun deserialize(bossPacked: Any): Any

    companion object {
        fun converters(): Iterable<SpecificConverter> = platfrmSpecificConverters
    }
}

expect val platfrmSpecificConverters: Iterable<SpecificConverter>

/**
 * Deserialization of the boss-encoded object. Note that root object passed to the instance
 * should be a map.
 *
 * Normally, you use variants of [decodeFrom] rather than instantiating this class directly or
 * extension functions [ByteArray.decodeBoss] and [Bossk.Reader.read].
 */
@ExperimentalSerializationApi
@OptIn(InternalSerializationApi::class)
class BossDecoder(
    private val currentObject: Map<String, Any?>,
    descriptor: SerialDescriptor,
) : NamedValueDecoder() {

    private var currentIndex = 0
    private val isCollection = descriptor.kind == StructureKind.LIST || descriptor.kind == StructureKind.MAP
    private val size = if (isCollection) Int.MAX_VALUE else descriptor.elementsCount

    @Suppress("UNCHECKED_CAST")
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return when (descriptor.kind) {
            is StructureKind.LIST -> BossListDecoder(currentObject[currentTag] as List<Any?>)
            is StructureKind.CLASS -> currentTagOrNull?.let {
                checkTagIsStored(it)
                BossDecoder(currentObject[it] as Map<String, Any?>, descriptor)
            } ?: this
            is StructureKind.OBJECT -> {
                if(descriptor.serialName == "kotlin.Unit")
                    this
                else
                    throw SerializationException("unsupported kind: ${descriptor.kind}")
            }
            else -> throw SerializationException("unsupported kind: ${descriptor.kind}")
        }
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (currentIndex < size) {
            val name = descriptor.getTag(currentIndex++)
            if (name in currentObject)
                return currentIndex - 1
            if (isCollection)
                break
        }
        return CompositeDecoder.DECODE_DONE
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        for( c in SpecificConverter.converters() ) {
            if( c.descriptor == deserializer.descriptor ) {
                return c.deserialize(decodeTaggedValue(currentTag)) as T
            }
        }
        return when (deserializer.descriptor) {
            byteArraySerializerDescriptor -> decodeTaggedValue(currentTag) as T
            Instant.serializer().descriptor -> decodeTaggedValue(currentTag) as T
            bossStructSerializerDescriptor -> {
                BossStruct(decodeTaggedValue(currentTag) as MutableMap<String, @Contextual Any?>) as T
            }
            else -> super.decodeSerializableValue(deserializer)
        }
    }

    override fun decodeTaggedNotNullMark(tag: String): Boolean =
        tag in currentObject && currentObject[tag] != null

    override fun decodeTaggedEnum(tag: String, enumDescriptor: SerialDescriptor): Int {
        return decodeTaggedInt(tag)
    }

    override fun decodeTaggedValue(tag: String): Any {
        checkTagIsStored(tag)
        return currentObject[tag]!!
    }

    override fun decodeTaggedFloat(tag: String): Float {
        return (decodeTaggedValue(tag) as Number).toFloat()
    }

    override fun decodeTaggedInt(tag: String): Int {
        return (decodeTaggedValue(tag) as Number).toInt()
    }

    override fun decodeTaggedLong(tag: String): Long {
        return (decodeTaggedValue(tag) as Number).toLong()
    }

    private fun checkTagIsStored(tag: String) {
        if (tag !in currentObject) throw SerializationException("missing property $tag")
    }

    companion object {
        internal val byteArraySerializerDescriptor = serializer<ByteArray>().descriptor
        internal val bossStructSerializerDescriptor = serializer<BossStruct>().descriptor

        /**
         * Decode (deserialize) from a reader. The return type could be specified as nullable.
         */
        suspend inline fun <reified T> decodeFrom(br: Bossk.Reader): T =
            decodeFrom(typeOf<T>(), br)

        suspend fun <T: Any> decodeFrom(cls: KType,br: Bossk.Reader): T {
            if( cls == typeOf<Map<*,*>>() )
                return br.read() as T
            val d = EmptySerializersModule.serializer(cls)
            val decoder = BossDecoder(br.read(), d.descriptor)
            return d.deserialize(decoder) as T
        }

        /**
         * Decode (deserialize) from a map, usually, returned by some boss decoder; for example, if you
         * have an array with different items in it, you can get ot with [Bossk.unpack] and then decode
         * each element with proper type:
         * ~~~
         * val list = Bossk.unpack(someData) as List<Any>
         * val element5: Element5 = BossDecoder.decodeFrom(list.structAt(5))
         * val element3: Element3 = BossDecoder.decodeFrom(list.structAt(3))
         * ~~~
         * as it is not possible in Kotlin to specify different types for array indexes, it is the only
         * way to easily handle polymorphic content arrays.
         *
         * @param map a map, or a [BossStruct] instance.
         * @return deserialized instance
         */
        inline fun <reified T> decodeFrom(map: Map<String,Any?>): T = decodeFrom(typeOf<T>(), map)

        fun <T> decodeFrom(cls: KType,map: Map<String,Any?>): T {
            if( cls == typeOf<BossStruct>() )
                return BossStruct(map.toMutableMap<String,Any?>()) as T
            val d = EmptySerializersModule.serializer(cls)
            val decoder = BossDecoder(BossStruct.from(map), d.descriptor)
            return d.deserialize(decoder) as T
        }

        /**
         * Decode (deserialize) from a byte array. The return type could be specified as nullable.
         */
        inline suspend fun <reified T> decodeFrom(binaryData: ByteArray): T {
            return decodeFrom(Bossk.Reader(binaryData.openChannel()))
        }
    }
}


@OptIn(ExperimentalSerializationApi::class)
internal class BossListDecoder(
    source: List<Any?>,
) : AbstractDecoder() {

    override val serializersModule = EmptySerializersModule

    private val values = source.iterator()
    private val size = source.size
    private var currentIndex = -1

    private var useCachedValue = false
    private var cache: Any? = null

    override fun decodeSequentially(): Boolean = true

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = size

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        if (values.hasNext()) ++currentIndex else CompositeDecoder.DECODE_DONE

    override fun decodeValue(): Any {
        return if (useCachedValue) {
            useCachedValue = false
            cache!!
        } else values.next()!!
    }

    override fun decodeNotNullMark(): Boolean {
        useCachedValue = true
        cache = values.next()
        return cache != null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        for( c in SpecificConverter.converters() ) {
            if( c.descriptor == deserializer.descriptor ) {
                return c.deserialize(decodeValue()) as T
            }
        }
        return when (deserializer.descriptor) {
            BossDecoder.byteArraySerializerDescriptor -> decodeValue() as T
            else -> super.decodeSerializableValue(deserializer)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (!values.hasNext())
            throw SerializationException("expected serialized class data missing")
        return when (descriptor.kind) {
            StructureKind.CLASS, PolymorphicKind.SEALED -> BossDecoder(values.next() as Map<String, Any?>, descriptor)
            StructureKind.LIST -> BossListDecoder(values.next() as List<Any?>)
            else -> throw SerializationException("unsupported kind: ${descriptor.kind}")
        }
    }
}
