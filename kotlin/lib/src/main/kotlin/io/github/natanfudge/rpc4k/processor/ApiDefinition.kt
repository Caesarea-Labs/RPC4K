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
data class RpcModel(val name: String, val typeParameters: List<String> = listOf(), val properties: Map<String, RpcType>)


/**
 * This class is similar to [RpcClass] except it doesn't have the concept of package names, and it does have the concept of type argument types,
 * which makes it more fitting for [RpcModel]s
 */
@Serializable
data class RpcType(val name: String, val isTypeParameter: Boolean = false, val typeArguments: List<RpcType> = listOf()) {
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

    //    // Inner classes are dollar seperated
//     val qualifiedDollarName get() = "$packageName.${simpleName.replace(".", "$")}"
    @Transient
    val className = ClassName(packageName, simpleName)

    @Transient
    val typeName: TypeName = className.let { name ->
        if (typeArguments.isEmpty()) name else name.parameterizedBy(typeArguments.map { it.typeName })
    }.copy(nullable = isNullable)
    val isUnit get() = packageName == "kotlin" && simpleName == "Unit"
}

@Serializable
@SerialName("AnotherModel")
private data class RpcTypeSurrogate(val name: String, val optional: Boolean = false, val typeArguments: List<RpcTypeSurrogate> = listOf())

private fun RpcClass.toSurrogate(): RpcTypeSurrogate =
    RpcTypeSurrogate(name = simpleName, optional = isNullable, typeArguments = typeArguments.map { it.toSurrogate() })

private fun RpcTypeSurrogate.toActual(packageName: String): RpcClass =
    RpcClass(packageName = packageName, simpleName = name, isNullable = optional, typeArguments = typeArguments.map { it.toActual(packageName) })

//TODO: handle kotlin code generation from non-kotlin sources
private const val GeneratedModelsPackage = "io.github.natanfudge.generated.models"

class RpcTypeSerializer : KSerializer<RpcClass> {
    override val descriptor = RpcTypeSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): RpcClass {
        return decoder.decodeSerializableValue(RpcTypeSurrogate.serializer()).toActual(GeneratedModelsPackage)
    }

    override fun serialize(encoder: Encoder, value: RpcClass) {
        encoder.encodeSerializableValue(RpcTypeSurrogate.serializer(), value.toSurrogate())
    }
}
