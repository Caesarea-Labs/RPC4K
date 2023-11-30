package io.github.natanfudge.rpc4k.runtime.api.components

import io.github.natanfudge.rpc4k.runtime.api.SerializationFormat
import io.github.natanfudge.rpc4k.runtime.implementation.serializers.Rpc4kSerializersModule
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.plus


/**
 * [JsonConfiguration.allowStructuredMapKeys] is not supported.
 */
class JsonFormat(config: JsonBuilder.() -> Unit = {}) : SerializationFormat {
    private val json = Json {
        config()
//        // NiceToHave: Support optional parameters and properties
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
