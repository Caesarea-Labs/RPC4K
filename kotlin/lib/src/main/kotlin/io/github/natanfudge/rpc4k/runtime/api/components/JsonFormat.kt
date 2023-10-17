package io.github.natanfudge.rpc4k.runtime.api.components

import io.github.natanfudge.rpc4k.runtime.api.Rpc4kSerializersModule
import io.github.natanfudge.rpc4k.runtime.api.SerializationFormat
import io.github.natanfudge.rpc4k.runtime.implementation.HeterogeneousListSerializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.internal.decodeStringToJsonTree
import kotlinx.serialization.modules.overwriteWith

/**
 * [JsonConfiguration.allowStructuredMapKeys] is not supported.
 */
class JsonFormat(config: JsonBuilder.() -> Unit = {}) : SerializationFormat {
    private val json = Json {
        config()
        // This should be done by every format
        serializersModule = serializersModule.overwriteWith(Rpc4kSerializersModule)
    }

    private val encoding = Charsets.UTF_8

    override fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray {
        return json.encodeToString(serializer, value).toByteArray(encoding)
    }

    override fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T {
        return json.decodeFromString(serializer, raw.toString(encoding))
    }
}