package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
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


/**
 * This type describes the bare minimum information we need about a type in order to generated things such as KSerializers for it.
 * This class is not expected to be directly instantiated from a text format, but rather the runtime that needs the RpcType should infer the RpcType
 * from the JVM context. In KSP for example all type information is available, so it's easy to produce this class
 * but when reading from RPC text files the generator will need to do some resolving to instantiate this.
 *
 * @param simpleName expected to exist in text format
 * @param isNullable expected to exist in text format
 * @param packageName expected to be inferred from context
 * @param typeArguments expected to exist partially in text format (need to see if it's even possible with non-lists and such)
 */
data class RpcType(val packageName: String, val simpleName: String, val isNullable: Boolean, val typeArguments: List<RpcType>) {
    val qualifiedName = "$packageName.$simpleName"
    val className = ClassName(packageName, simpleName)
    val typeName: TypeName = className.let { name ->
        if (typeArguments.isEmpty()) name else name.parameterizedBy(typeArguments.map { it.typeName })
    }.copy(nullable = isNullable)
    val isUnit get() = packageName == "kotlin" && simpleName == "Unit"
}


class RpcTypeSerializer : KSerializer<RpcType> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): RpcType {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: RpcType) {
        TODO()
    }
}
