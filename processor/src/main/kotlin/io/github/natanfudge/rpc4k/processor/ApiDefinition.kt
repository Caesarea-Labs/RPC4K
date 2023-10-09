package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.natanfudge.rpc4k.processor.utils.nonNullQualifiedName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * @param implementationPackageName Since the generated API server needs to reference the implementation of the server, we need to know
 * where the server implementation is.
 */
@Serializable
data class ApiDefinition(val name: String, val implementationPackageName: String, val methods: List<RpcDefinition>)

@Serializable
data class RpcDefinition(val name: String, val args: List<RpcArgumentDefinition>, @Serializable(RpcTypeSerializer::class) val returnType: RpcType)

@Serializable
data class RpcArgumentDefinition(val name: String, @Serializable(RpcTypeSerializer::class) val type: RpcType)

sealed interface RpcType {
    fun asTypeName(): TypeName
    class Ksp(val value: KSTypeReference) : RpcType {
        override fun asTypeName(): TypeName {
            return value.toTypeName()
        }
    }
}


class RpcTypeSerializer : KSerializer<RpcType> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): RpcType {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: RpcType) {
        val string = when (value) {
            is RpcType.Ksp -> value.value.resolve().nonNullQualifiedName()
        }
        encoder.encodeString(string)
    }
}