package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.TypeName
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class ApiDefinition(val name: String, val methods: List<RpcDefinition>)

@Serializable
data class RpcDefinition(val name: String, val args: List<RpcArgumentDefinition>, @Serializable(RpcTypeSerializer::class) val returnType: RpcType)

@Serializable
data class RpcArgumentDefinition(val name: String, @Serializable(RpcTypeSerializer::class) val type: RpcType)

typealias RpcType = TypeName

class RpcTypeSerializer : KSerializer<RpcType> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): RpcType {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: RpcType) {
        encoder.encodeString(value.toString())
    }
}