# boss-serialization-mp

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Multiplatform lightweight BOSS format and kotlinx serialization support. Boss codec is completely rewritten in kotlin, providing:

> It is yet in active testing, consider beta stage. Please report any bugs found.

- coroutine support: Boss codec uses 'Channel<Byte>' to decode and encode, on every platofrm Very fast.
- no more big dependencies!

Due to multiplatform nature it has several differences from java version:

- type for Boss's datetime is `kotlinx.datetime.Instant`: crossplatofrm datetime library.
- type for Boss's BigInteger is `com.ionspin.kotlin.bignum.integer.BigInteger`: it is multiplatform and as close to java/kotlin BigInteger as I was able to find

## Crossplatorm notes

On JS platofrm when encoding it is not always possible to distinguish between double and integer/long values, so +1.0 will be encoded to int +1, while 1.211 will be rpoperly encoded to a double. Decoding works well.

It is necessary to properly use `kotlinx.datetimme.Instant` instead of java variants. If, by mistake, the java standard library instant will be used, it will be silently serialized by first available serializer and most likely will be encoded as a String in boss binary, not the expected datetime boss type. This issue might be addressed in the future. To cope with it there is a supplied `ZonedDateTimeConverter` that could be used to convert from/to `ZonedDateTime`, and it could be extended also to fix unexpected `Instant`.

## General information

kotlinx.serialization module for [BOSS](https://kb.universablockchain.com/boss_serialization_protocol/307) space-effective typed binary format used in most Universa and [iCodici](https://icodici.com) products and other places. As for now it provides boss-compliant de/serialization of:

- fields of primitive nullable and non-nullable types: Int, Long, Boolean, Double. Note that boss' uses vairable length integers using minimum necessary bytes to keeo the integer despite of its type. E.g. +1, +1L will occupy only one byte in a binary pack, a standard practice for boss. E.g. you should not mind using longs as it will be compressed.

- boss-specific, cached serialization for ByteArray as boss binary data

- boss-specific cached strings

- Maps (using `BossStruct` class instances)

- Lists

- enums are serialized _using ordinals to reduce length_. This goes along with Boss philosophy.

Note that it is based on experimental API of the kotlinx serialization that could be changed, so it is also experimental.

## How to use

Add our public repository to your project:

~~~
repositories {
    // google() or whatever else
    // ...
    maven("https://maven.universablockchain.com/")
}
~~~

Add dependency to where needed:

~~~
dependencies {
    //...  
    implementation("net.sergeych:boss-serialization-mp:0.0.1")
}
~~~

## Usage notes

As for now, you should copy from releases (or build from sources) the jar and install it into your project. There is a gradle task to build and copy jars into shared space `$projectRoot\..\jarlib\boss-serialization.jar`. From there it could  be included into other projects somewhat like:

~~~
implementation(files("../jarlib/kotyara.jar"))
~~~

Sorry for inconvenience, we'll push it to some maven repository as soon as it gets more tested.

## Deserialization

~~~
val binaryData: ByteArray
val decoded: MyDataClass = binaryData.decodeBoss()
~~~

Also, you can decode from `Boss.Reader` _stream_:

~~~
val r = Boss.Reader(inputStream)
val decoded: MyDataClass = r.deserialize()
~~~

__Important note__. The root object of the data to decode _must be a boss map_. E.g. you can't deserialize primitive types and lists as root objects. These compound types could be contained in fields only, the root object should still be a map. In other ford, you decode root objects only to classes.

If you need to decode boss to a map rather than a class instance, use `BossStruct` for field types and `binaryData.decodeBossStruct`.

### Allowed field types

- simple types: `Int, Long, Float, Double, Boolean`
- enums (ordinals are used)
- `ZonedDateTime` for boss datetime field type
- `ByteArray` for boss binary field type
- `List<T>` for boss arrays, any serializable item type (null included)
- `BossStruct` for boss maps, string keys, any serializable content (null included)
- nullability is supported for all fields

## Important limitations

### No polymorphic Any-arrays in de/serializer

The `lotlinx.serialization` library is compile-time, no reflection, so it can't really distinguish types in polymorphic arrays if the base type for the component is Any. So, use traditional Boss class interface to cope with such. Still, if the type of the array is not Any, you can include it providing you registered de/serializer for your base class that implements such polymorphism, it is an allowed and almost well described technique, see main library docs.

### Root object should be a class instance, not a collection 

...and not a simple type. You can not serialize or deserialize a list, or a simple type, or whatever else as a root object. You _can serialize it as a field of a root object. The object you serialize should always be a class instance, and the object you deserialize to should be an instance of a class, not a list or a map. The fields could be lists, maps or whatever else, but the root object should be a class instance. It means that in the encoded boss object the root object must be a map.

Please add an issue if you really need to use anything at root level.

### Use `BossStruct` as a Map in fields

Note that due to some design limitation of `kotlinx.serialization` library you __must use `BossStruct`__ wherever you need a Map. It is a wrap around a `Map<String,Any?>` that allows library to properly deserialize it.

### Use provided `ZonedDateTimeSerializer`  

Boss format natively supports timestamps, to it is important to use it. This library relies on serialization
descriptor of the mentioned above serializer. Using other serializers will break binary compatibility of boss format which successfully runs on various platforms. 

You can, for example, add the following line

```
@file:UseSerializers(ZonedDateTimeSerializer::class)
```

to the beginning any file that uses `ZonedDateTime` serializable fields.

## Notes on the security

Our repository accounts credentials are secured with [zetext encryption](https://github.com/sergeych/ZeText): we care about our users and are trying to protect against credential stealing and supply chain attack. Feel free to use this approach too.

Also, we DO NOT USE APACHE LOG4J (and actually neved used it, 20+M to write text to a log file is absurd).

## Boss protocol links

### Java Boss library

~~~
repositories { 
    maven( "https://maven.universablockchain.com") 
}
dependencies {
    implementation("com.icodici:common_tools:3.14.3+")
}
~~~

### Javascript support

There is a NPM module [Unicrypto](https://www.npmjs.com/package/unicrypto) that works well in both browser and nodejs environment. It is widely used and production stable.

### Ruby support

There is stable and production-ready ruby gem for it: [sergeych/boss_protocol](https://github.com/sergeych/boss_protocol). This gem is in use in many production environments.

### C++

There is a BOSS codec in the [Universa U8 engine](https://github.com/UniversaBlockchain/U8) but it may not be easy to extract it from there.

## License

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
