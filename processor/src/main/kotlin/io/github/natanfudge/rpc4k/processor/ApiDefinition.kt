package io.github.natanfudge.rpc4k.processor

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class ApiDefinition(val name: String, val methods: RpcDefinition)

@Serializable
data class RpcDefinition(val name: String, val args: List<RpcArgumentDefinition>, @Serializable(RpcTypeSerializer::class) val returnType: RpcType)

@Serializable
data class RpcArgumentDefinition(val name: String, @Serializable(RpcTypeSerializer::class) val type: RpcType)

typealias RpcType = Class<@Contextual Any?>

class RpcTypeSerializer : KSerializer<Class<*>> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): Class<*> {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: Class<*>) {
        encoder.encodeString(value.simpleName)
    }
}