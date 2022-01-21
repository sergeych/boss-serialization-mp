package net.sergeych.boss_serialization_mp

import kotlinx.datetime.Instant
import net.sergeych.boss_serialization.BossDecoder
import net.sergeych.bossk.Bossk
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Platform-independent storage capable of storing binary data for string keys.
 */
interface KVBinaryStorage {
    operator fun get(key: String): ByteArray?
    operator fun set(key: String, value: ByteArray)
    fun remove(key: String): ByteArray?
    val keys: Set<String>
    fun clear() {
        for (k in keys) remove(k)
    }
}

/**
 * Smart storage for arbitrary typed data that uses BOSS encoding for primitive types and kotlinx serialization
 * for everything else, then store its binary represetation into storage provided
 *
 * @param storage storage for serialized/packed data
 */
class KVStorage(val storage: KVBinaryStorage) {

    inline operator fun <reified T> get(key: String): T? = get(typeOf<T>(), key)

    fun <T> get(type: KType, key: String): T? {
        val s = storage[key] ?: return null
        val value = Bossk.unpack<Any>(s)
        return (when (value) {
            is String, is Number, is Boolean, is Instant, is ByteArray -> value
            is Map<*, *> -> BossDecoder.decodeFrom<T>(type, value as Map<String, Any?>)
            else -> throw IllegalArgumentException("can't unpack, unknown type")
        }) as T
    }

    inline operator fun <reified T> set(key: String, value: T?) = set(typeOf<T>(), key, value)

    inline fun set(type: KType, key: String, value: Any?) {
        when (value) {
            null -> storage.remove(key)
            is String, is Number, is Boolean, is Instant, is ByteArray -> storage[key] = Bossk.pack(value)
            else -> storage[key] = Bossk.pack(BossEncoder.encodeToStruct(type, value))
        }
    }

    fun delete(key: String) {
        storage.remove(key)
    }

    inline fun <reified T> remove(key: String): T? = get<T>(key)?.also { storage.remove(key) }

    fun clear() {
        storage.clear()
    }

    val keys: Set<String>
        get() = storage.keys
}

// Alas this one does not fowk with JS 1.6.10... waiting...
//
//abstract class PropBase<T>(overrideName: String? = null) : ReadWriteProperty<Any?,T> {
//    private var effectiveName = overrideName
//
//    protected fun name(property: KProperty<*>): String {
//        if (effectiveName == null) {
//            effectiveName = property.name
//        }
//        return effectiveName!!
//    }
//}
//inline fun <reified T>kvOptStorage(storage: KVStorage,overrideName: String? = null): ReadWriteProperty<Any?, T?> {
//    return object : PropBase<T?>(overrideName) {
//        val type = typeOf<T>()
//        override operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
//            return storage.get(type, name(property))
//        }
//        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
//            storage.set(name(property), value)
//        }
//    }
//}
// this is ugly and RY (not DRY) but the fucking JS-legacy fails to compile nice and beautil and compacl variant
// that ised object expressions:

/**
 * Non-nullable property delefate for [KVStorage], compatible with buggy kotlin.js 1.6.10 compiler. Do not use this
 * class directly, we will remove it ASAP when kotlin js compiler will be less stupid. Please use [kvStorage]
 * instead
 */
class KVStorageDelegate<T>(val type: KType, val storage: KVStorage, overrideName: String? = null) {

    private var effectiveName = overrideName

    protected fun name(property: KProperty<*>): String {
        if (effectiveName == null) {
            effectiveName = property.name
        }
        return effectiveName!!
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return storage.get(type, name(property))!!
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        storage.set(type, name(property), value)
    }
}

/**
 * Nullable property delefate for [KVStorage], compatible with buggy kotlin.js 1.6.10 compiler. Do not use this
 * class directly, we will remove it ASAP when kotlin js compiler will be less stupid. Please use [optKvStorage]
 * instead
 */
class KVStorageNullableDelegate<T>(val type: KType, val storage: KVStorage, overrideName: String? = null) {

    private var effectiveName = overrideName

    protected fun name(property: KProperty<*>): String {
        if (effectiveName == null) {
            effectiveName = property.name
        }
        return effectiveName!!
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return storage.get(type, name(property))
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        storage.set(type, name(property), value)
    }
}

/**
 * The property delegate to access data from the [KVBinaryStorage]. Data must exist (or must be assigned before
 * any use).
 * @param storage where to store data
 * @param overrideName if present, set the key for stored data
 */
inline fun <reified T> kvStorage(storage: KVStorage, overrideName: String? = null): KVStorageDelegate<T> {
    return KVStorageDelegate<T>(typeOf<T>(), storage, overrideName)
}

/**
 * The property delegate to access data in the [KVStorage] in nullable way, where null means storage has no
 * data for this property:
 * ~~~
 *   val nullableName by optKvStorage<MyType>(s, "nulx")
 *   // or
 *   val nullableName2: MyType? by optKvStorage<MyType>(s, "nulx")
 * ~~~
 * please note that the type should be __explicitly specified as a generic parameter of the function__ otherwise
 * kotlin compiler (as of 1.6.10) will not properly deduce types. Note also that the parameter type can't be
 * nullable itself.
 *
 * @param storage where to store data
 * @param overrideName if present, set the key for stored data
 */
inline fun <reified T> optKvStorage(
    storage: KVStorage,
    overrideName: String? = null
): KVStorageNullableDelegate<T> {
    return KVStorageNullableDelegate<T>(typeOf<T>(), storage, overrideName)
}

expect fun DefaultBinaryStorage(storageName: String): KVBinaryStorage

