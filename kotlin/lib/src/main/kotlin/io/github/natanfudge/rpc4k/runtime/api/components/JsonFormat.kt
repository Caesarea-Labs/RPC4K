package io.github.natanfudge.rpc4k.runtime.api.components

import io.github.natanfudge.rpc4k.runtime.api.SerializationFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * [JsonConfiguration.allowStructuredMapKeys] is not supported.
 */
class JsonFormat(private val json: Json = Json) : SerializationFormat {
    private val encoding = Charsets.UTF_8

    override fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray {
        return json.encodeToString(serializer, value).toByteArray(encoding)
    }

    override fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T {
        return json.decodeFromString(serializer, raw.toString(encoding))
    }
}