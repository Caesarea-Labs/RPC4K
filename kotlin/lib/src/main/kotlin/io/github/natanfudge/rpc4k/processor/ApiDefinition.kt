package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


/**
 * @param name The type of this is not exactly accurate, as it's always not nullable and with no type arguments.
 */
@Serializable
data class ApiDefinition(val name: RpcClass, val methods: List<RpcDefinition>, val models: List<RpcModel>)

@Serializable
data class RpcDefinition(val name: String, val parameters: List<RpcParameter>, val returnType: RpcClass)

@Serializable
data class RpcParameter(val name: String, val type: RpcClass)
@Serializable
sealed interface RpcModel {
    val name: String
    //TODO: consider whether or not it's worth having default values on these things. The benefit is that it often reduces size by like 4x
    // the downside is that it's more work on clients to write down what the default value is.
    // Need to consider the fact we might want multiple api versions floating around sitting in the server's DB.
    @Serializable
    @SerialName("struct")
    data class Struct(override val name: String, val typeParameters: List<String>, val properties: Map<String, RpcType>): RpcModel
    @Serializable
    @SerialName("enum")
    data class Enum(override val name: String, val options: List<String>): RpcModel

    /**
     * Important note: Languages implementing this MUST add a `type: ` field to the structs referenced by `options` so the other side
     * may know which of the union options a union value is
     * @param [options] a list of possible types this union can evaluate to.
     */
    @Serializable
    @SerialName("union")
    data class Union(override val name: String, val options: List<RpcType>, val typeParameters: List<String>): RpcModel
}
//TODO: make the `_type` property name reserved, because we sometimes need it for union types, and configure json to use _type.


/**
 * This class is similar to [RpcClass] except it doesn't have the concept of package names, and it does have the concept of type argument types,
 * which makes it more fitting for [RpcModel]s
 */
@Serializable
data class RpcType(val name: String, val isTypeParameter: Boolean, val isOptional: Boolean, val typeArguments: List<RpcType>) {
    init {
        // Kotlin doesn't have higher-kinded types yet
        if (isTypeParameter) check(typeArguments.isEmpty())
    }
}


/**
 * This type describes the bare minimum information we need about a type in order to generate things such as KSerializers for it.
 * This class is not expected to be directly instantiated from a text format, but rather the runtime that needs the RpcType should infer the RpcType
 * from the JVM context. In KSP for example all type information is available, so it's easy to produce this class
 * but when reading from RPC text files the generator will need to do some resolving to instantiate this.
 *
 * @param simpleName expected to exist in text format
 * @param isNullable expected to exist in text format
 * @param packageName expected to be inferred from context
 * @param typeArguments expected to exist in text format
 */
@Serializable(with = RpcTypeSerializer::class)
data class RpcClass(val packageName: String, val simpleName: String, val isNullable: Boolean, val typeArguments: List<RpcClass>) {
    // Inner classes are dot seperated
    @Transient
    val qualifiedName = "$packageName.$simpleName"

    @Transient
    val className = ClassName(packageName, simpleName)

    @Transient
    val typeName: TypeName = className.let { name ->
        if (typeArguments.isEmpty()) name else name.parameterizedBy(typeArguments.map { it.typeName })
    }.copy(nullable = isNullable)
    val isUnit get() = packageName == "kotlin" && simpleName == "Unit"
}

/**
 * Exist purely to serialize [RpcClass]s
 */
@Serializable
private data class RpcClassSurrogate(val name: String, val isOptional: Boolean, val typeArguments: List<RpcClassSurrogate>)

private fun RpcClass.toSurrogate(): RpcClassSurrogate =
    RpcClassSurrogate(name = simpleName, isOptional = isNullable, typeArguments = typeArguments.map { it.toSurrogate() })

private fun RpcClassSurrogate.toActual(packageName: String): RpcClass =
    RpcClass(packageName = packageName, simpleName = name, isNullable = isOptional, typeArguments = typeArguments.map { it.toActual(packageName) })

//TODO: handle kotlin code generation from non-kotlin sources
private const val GeneratedModelsPackage = "io.github.natanfudge.generated.models"

class RpcTypeSerializer : KSerializer<RpcClass> {
    override val descriptor = RpcClassSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): RpcClass {
        return decoder.decodeSerializableValue(RpcClassSurrogate.serializer()).toActual(GeneratedModelsPackage)
    }

    override fun serialize(encoder: Encoder, value: RpcClass) {
        encoder.encodeSerializableValue(RpcClassSurrogate.serializer(), value.toSurrogate())
    }
}
