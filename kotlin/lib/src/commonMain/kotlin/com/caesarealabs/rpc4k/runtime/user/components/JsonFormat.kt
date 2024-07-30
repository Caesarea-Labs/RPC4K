package com.caesarealabs.rpc4k.runtime.user.components

import com.caesarealabs.rpc4k.runtime.api.SerializationFormat
import com.caesarealabs.rpc4k.runtime.implementation.serializers.Rpc4kSerializersModule
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.plus


/**
 * [JsonConfiguration.allowStructuredMapKeys] is not supported.
 */
public class JsonFormat(config: JsonBuilder.() -> Unit = {}) : SerializationFormat {
    private val json = Json {
        config()
//        // NiceToHave: Support optional parameters and properties
        encodeDefaults = true
        // This should be done by every format
        serializersModule = Rpc4kSerializersModule + serializersModule
    }


    override fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray {
        return json.encodeToString(serializer, value).encodeToByteArray()
    }

    override fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T {
        return json.decodeFromString(serializer, raw.decodeToString())
    }
}
