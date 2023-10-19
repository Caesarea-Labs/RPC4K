package io.github.natanfudge.rpc4k.runtime.api.components

import io.github.natanfudge.rpc4k.runtime.api.Rpc4kSerializersModule
import io.github.natanfudge.rpc4k.runtime.api.SerializationFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.plus


//TODO: implement jsonFormatProvider

//TODO: I give up: change generated classes to use one GeneratedClassConfig instead of many properties.

//TODO: make this a private constructor with a a serializersModule parameter and move to doing everything through a serializationFormatProvider.
//

//class JsonFormat private constructor(private val json: Json) : SerializationFormat {
//    //TODO: consider renaming this to just "JsonFormat" and making the OG JsonFormat an implementation detail named JsonFormatImpl or some shit.
//    // Then I could rename SerializationFormat to ConfiguredSerializationFormat and rename SerializationFormatProvider to SerializationFormat.
//    class Provider(private val config: JsonBuilder.() -> Unit = {}) : SerializationFormatProvider<JsonFormat> {
//        override fun provide(module: SerializersModule): JsonFormat {
//            return JsonFormat(Json {
//                config()
//                //TODO: It pains me to do this but there's no easy way to detect that a property has a default value, so we can't tell clients to
//                // expect 'no value' when the default value is omitted.
//                // In the future with better default handling we can get rid of this.
//                encodeDefaults = true
//                // This should be done by every format
//                serializersModule = module + serializersModule
//            })
//        }
//    }
/**
 * [JsonConfiguration.allowStructuredMapKeys] is not supported.
 */
class JsonFormat(config: JsonBuilder.() -> Unit = {}) : SerializationFormat {
    private val json = Json {
        config()
        //TODO: It pains me to do this but there's no easy way to detect that a property has a default value, so we can't tell clients to
        // expect 'no value' when the default value is omitted.
        // In the future with better default handling we can get rid of this.
        encodeDefaults = true
        // This should be done by every format
        serializersModule = Rpc4kSerializersModule + serializersModule
    }

    private val encoding = Charsets.UTF_8

    override fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray {
        return json.encodeToString(serializer, value).toByteArray(encoding)
    }

    override fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T {
        return json.decodeFromString(serializer, raw.toString(encoding))
    }
}
