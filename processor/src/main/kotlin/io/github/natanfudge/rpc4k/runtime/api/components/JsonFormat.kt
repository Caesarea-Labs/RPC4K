package io.github.natanfudge.rpc4k.runtime.api.components

import io.github.natanfudge.rpc4k.runtime.api.SerializationFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

// This is in UTF-8/ascii encoding
private const val openingBraceByte: Byte = 91 // [
private const val closingBraceByte: Byte = 93 // ]
private const val commaByte: Byte = 44 // ,

class JsonFormat(private val json: Json = Json) : SerializationFormat {
    private val encoding = Charsets.UTF_8

    override fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray {
        return json.encodeToString(serializer, value).toByteArray(encoding)
    }

    override fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T {
        return json.decodeFromString(serializer, raw.toString(encoding))
    }

//    override fun combine(values: List<ByteArray>): ByteArray {
//        // + 1 for each ',' , + 2 for '[]', - 1 for the fact a comma is not present before the first element.
//        val resultArraySize = values.sumOf { it.size + 1 } + 2 - 1
//        val resultArray = ByteArray(resultArraySize)
//        resultArray[0] = openingBraceByte
//        var pos = 1
//        for(value in values) {
//            resultArray.enc
//        }
//        TODO()
////        val resultArray =
//    }
}