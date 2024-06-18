package com.caesarealabs.rpc4k.runtime.implementation.serializers

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder



/**
 * 128 bit ints are represented normally as a string.
 */
public object UUIDSerializer : KSerializer<Uuid> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): Uuid {
        val str = decoder.decodeString()
        return try {
            uuidFrom(str)
        } catch (e: IllegalArgumentException) {
            throw SerializationException("Invalid UUID string '$str'", e)
        }
    }

    override fun serialize(encoder: Encoder, value: Uuid) {
        encoder.encodeString(value.toString())
    }
}
