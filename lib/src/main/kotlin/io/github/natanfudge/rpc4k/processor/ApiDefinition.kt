package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
@Serializable
data class RpcType(val packageName: String, val simpleName: String, val isNullable: Boolean = false, val typeArguments: List<RpcType> = listOf()) {
    // Inner classes are dot seperated
    @Transient val qualifiedName = "$packageName.$simpleName"
    // Inner classes are dollar seperated
     val qualifiedDollarName get() = "$packageName.${simpleName.replace(".", "$")}"
    @Transient val className = ClassName(packageName, simpleName)
    @Transient val typeName: TypeName = className.let { name ->
        if (typeArguments.isEmpty()) name else name.parameterizedBy(typeArguments.map { it.typeName })
    }.copy(nullable = isNullable)
    val isUnit get() = packageName == "kotlin" && simpleName == "Unit"
}

@Serializable
private data class RpcTypeSurrogate(val name: String, val optional: Boolean = false, val typeArguments: List<RpcTypeSurrogate> = listOf())

private fun RpcType.toSurrogate(): RpcTypeSurrogate =
    RpcTypeSurrogate(name = simpleName, optional = isNullable, typeArguments = typeArguments.map { it.toSurrogate() })

class RpcTypeSerializer : KSerializer<RpcType> {
    override val descriptor = RpcTypeSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): RpcType {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: RpcType) {
        encoder.encodeSerializableValue(RpcTypeSurrogate.serializer(), value.toSurrogate())
    }
}

//TODO: I've figured out the model for RPCs.
// The server is the source of truth.
// The APIs are defined the in the server source, they generate a json, and client handlers are generated from the json.
// Additionally, the server defines data classes, and data classes are generated for the respective client handlers.
// That's it. Pretty simple. For bi-directional communication we allow both sides to define an API for speaking to them, and the user must generate bindings.