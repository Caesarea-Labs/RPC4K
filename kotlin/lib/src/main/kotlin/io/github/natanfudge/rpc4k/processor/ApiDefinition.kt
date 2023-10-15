package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapEntrySerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


/**
 * @param name The type of this is not exactly accurate, as it's always not nullable and with no type arguments.
 */
@Serializable
data class ApiDefinition(val name: KotlinTypeReference, val methods: List<RpcDefinition>, val models: List<RpcModel>)

@Serializable
data class RpcDefinition(val name: String, val parameters: List<RpcParameter>, val returnType: KotlinTypeReference)

@Serializable
data class RpcParameter(val name: String, val type: KotlinTypeReference)

@Serializable
sealed interface RpcModel {
    val name: String

    //TODO: consider whether or not it's worth having default values on these things. The benefit is that it often reduces size by like 4x
    // the downside is that it's more work on clients to write down what the default value is.
    // Need to consider the fact we might want multiple api versions floating around sitting in the server's DB.
    @Serializable
    @SerialName("struct")
    data class Struct(override val name: String, val typeParameters: List<String>, val properties: Map<String, KotlinTypeReference>) : RpcModel

    @Serializable
    @SerialName("enum")
    data class Enum(override val name: String, val options: List<String>) : RpcModel

    /**
     * Important note: Languages implementing this MUST add a `type: ` field to the structs referenced by `options` so the other side
     * may know which of the union options a union value is
     * @param [options] a list of possible types this union can evaluate to.
     */
    @Serializable
    @SerialName("union")
    data class Union(override val name: String, val options: List<KotlinTypeReference>, val typeParameters: List<String>) : RpcModel
}
//TODO: make the `_type` property name reserved, because we sometimes need it for union types, and configure json to use _type.


/**
 * This class is similar to [KotlinTypeReference] except it doesn't have the concept of package names, and it does have the concept of type argument types,
 * which makes it more fitting for [RpcModel]s
 */
@Serializable
data class RpcType(val name: String, val isTypeParameter: Boolean, val isOptional: Boolean, val typeArguments: List<RpcType>) {
    init {
        // Kotlin doesn't have higher-kinded types yet
        if (isTypeParameter) check(typeArguments.isEmpty())
    }

    companion object BuiltinNames {

        //TODO: consider supporting unsigned types if it's required enough
            const val Bool = "bool"
            const val I8 = "i8"
            const val I16 = "i16"
            const val I32 = "i32"
            const val I64 = "i64"
            const val F32 = "f32"
            const val F64 = "f64"
            const val Char = "char"
            const val String = "string"
            const val Void = "void"
            const val Array = "array"
            const val Record = "record"
            const val Tuple = "tuple"
    }
}


/**
 * More precise description of an [RpcType] that comes from the JVM, and makes it easier to generate kotlin code as it includes the package name of the type
 * and uses kotlin class names and not RPC class names
 */
@Serializable(with = RpcTypeSerializer::class)
data class KotlinTypeReference(val packageName: String, val simpleName: String, val isNullable: Boolean, val typeArguments: List<KotlinTypeReference>, val isTypeParameter: Boolean) {
    // Inner classes are dot seperated
    val qualifiedName = "$packageName.$simpleName"

    val className = ClassName(packageName, simpleName)

    val typeName: TypeName = className.let { name ->
        if (typeArguments.isEmpty()) name else name.parameterizedBy(typeArguments.map { it.typeName })
    }.copy(nullable = isNullable)
    val isUnit get() = packageName == "kotlin" && simpleName == "Unit"
}

 const val GeneratedModelsPackage = "io.github.natanfudge.generated.models"

/**
 * [KotlinTypeReference] is serialized to a [RpcType]
 */
class RpcTypeSerializer : KSerializer<KotlinTypeReference> {
    override val descriptor = RpcType.serializer().descriptor

    override fun deserialize(decoder: Decoder): KotlinTypeReference {
        return decoder.decodeSerializableValue(RpcType.serializer()).toKotlinTypeReference(GeneratedModelsPackage)
    }

    override fun serialize(encoder: Encoder, value: KotlinTypeReference) {
        encoder.encodeSerializableValue(RpcType.serializer(), value.toRpcType())
    }
}

private fun RpcType.toKotlinTypeReference(packageName: String): KotlinTypeReference = TODO("handle kotlin code generation from non-kotlin sources")

private fun KotlinTypeReference.toRpcType() = when (packageName) {
    "kotlin" -> toBuiltinRpcType()
    "kotlin.collections" -> toBuiltinCollectionType()
    else -> toUserType()
}


/**
 * Converts Kotlin types like Int to RPC types like i32.
 */
private fun KotlinTypeReference.toBuiltinRpcType(): RpcType {
    return when (simpleName) {
        "Boolean", "Byte", "UByte", "Short", "UShort", "Int", "UInt", "Long", "ULong", "Char", "String", "Unit", "Float", "Double" -> {
            // Primitive types
            // Unsigned types are treated the same as the normal types
            val name = when (simpleName) {
                "Boolean" -> RpcType.Bool
                "Byte", "UByte" -> RpcType.I8
                "Short", "UShort" -> RpcType.I16
                "Int", "UInt" -> RpcType.I32
                "Long", "ULong" -> RpcType.I64
                "Char" -> RpcType.Char
                "String" -> RpcType.String
                "Unit" -> RpcType.Void
                "Float" -> RpcType.F32
                "Double" -> RpcType.F64
                else -> error("Impossible class name $simpleName")
            }
            RpcType(name, isOptional = isNullable, isTypeParameter = false, typeArguments = listOf())
        }

        "ByteArray", "ShortArray", "IntArray", "LongArray", "CharArray", "UByteArray", "UShortArray", "UIntArray", "ULongArray" -> {
            // Primitive array types
            val typeArgumentName = when (simpleName) {
                // We expand Xarray into an array of the respective type X
                // Unsigned types are treated the same as the normal types
                "ByteArray", "UByteArray" -> RpcType.I8
                "ShortArray", "UShortArray" -> RpcType.I16
                "IntArray", "UIntArray" -> RpcType.I32
                "LongArray", "ULongArray" -> RpcType.I64
                "CharArray" -> RpcType.Char
                else -> error("Impossible class name $className")
            }
            val typeArgument = RpcType(typeArgumentName, isOptional = false, isTypeParameter = false, typeArguments = listOf())
            RpcType(RpcType.Array, isOptional = isNullable, isTypeParameter = false, typeArguments = listOf(typeArgument))
        }

        "Pair", "Triple", "Array" -> {
            // Types that use normal type arguments - pair, triple, array
            val name = if (simpleName == "Array") RpcType.Array else RpcType.Tuple
            RpcType(
                name = name,
                isOptional = isNullable,
                isTypeParameter = false,
                typeArguments = typeArguments.map { it.toRpcType() }
            )
        }

        else -> error(
            "Unexpected kotlin builtin type: ${className}." +
                    " Was a custom class declared in kotlin.*? This is probably a bug in RPC4K."
        )
    }
}

private fun KotlinTypeReference.toBuiltinCollectionType(): RpcType {
    val name = when (simpleName) {
        "List", "Set" -> RpcType.Array
        "Map" -> RpcType.Record
        //TODO: need to register different serializers for MapEntry, I want to serialize it as a tuple but currently it's serialized as a {key: , value: }
        // object. This requires changing the generated code to use my own MapEntrySerializer and registering my own serializer in the json serialzation context.
        "Map.Entry" -> RpcType.Tuple
        else -> error(
            "Unexpected kotlin builtin collection type: ${className}." +
                    " Was a custom class declared in kotlin.collections.*? This is probably a bug in RPC4K."
        )
    }

    return RpcType(
        name = name,
        isOptional = isNullable,
        isTypeParameter = false,
        typeArguments = typeArguments.map { it.toRpcType() }
    )
}


private fun KotlinTypeReference.toUserType(): RpcType {
    return RpcType(
        name = simpleName,
        isOptional = isNullable,
        isTypeParameter = isTypeParameter,
        typeArguments = typeArguments.map { it.toRpcType() }
    )
}