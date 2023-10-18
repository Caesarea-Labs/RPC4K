package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


//TODO: I think a cool feature would be to have a link to the server file in the clients in cases you have the server source
//TODO: javadoc/jsdoc generation


/**
 * @param name The type of this is not exactly accurate, as it's always not nullable and with no type arguments, so we serialize it to just a String.
 */
@Serializable
data class ApiDefinition(
    @Serializable(with = KotlinTypeReferenceNameSerializer::class) val name: KotlinTypeReference,
    val methods: List<RpcDefinition>,
    val models: List<RpcModel>
)

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
    data class Struct(override val name: String, val typeParameters: List<String> = listOf(), val properties: List<Property>) : RpcModel {
        @Serializable
        data class Property(val name: String, val type: KotlinTypeReference, val isOptional: Boolean = false)
    }

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
    data class Union(override val name: String, val options: List<KotlinTypeReference>, val typeParameters: List<String> = listOf()) : RpcModel
}



//TODO: make the `type` property name reserved, because we sometimes need it for union types
//TODO: add type property to any struct that is part of a union


/**
 * More precise description of an [RpcType] that comes from the JVM, and makes it easier to generate kotlin code as it includes the package name of the type
 * and uses kotlin class names and not RPC class names
 */
@Serializable(with = KotlinTypeReferenceSerializer::class)
data class KotlinTypeReference(
    val packageName: String,
    val simpleName: String,
    val isNullable: Boolean = false,
    // True in cases where the value is initialized by a default value
    val hasDefaultValue: Boolean = false,
    val typeArguments: List<KotlinTypeReference> = listOf(),
    val isTypeParameter: Boolean = false,
    val inlinedType: KotlinTypeReference? = null
) {
    companion object {
        val string = KotlinTypeReference("kotlin", "String")
    }

    // Inner classes are dot seperated
    val qualifiedName = "$packageName.$simpleName"

    val className = ClassName(packageName, simpleName)

    val typeName: TypeName = className.let { name ->
        if (typeArguments.isEmpty()) name else name.parameterizedBy(typeArguments.map { it.typeName })
    }.copy(nullable = isNullable)
    val isUnit get() = packageName == "kotlin" && simpleName == "Unit"
}

/**
 * This class is similar to [KotlinTypeReference] except it doesn't have the concept of package names, and it does have the concept of type argument types,
 * which makes it more fitting for [RpcModel]s
 */
@Serializable
data class RpcType(
    val name: String,
    val isTypeParameter: Boolean = false,
    val isNullable: Boolean = false,
    val typeArguments: List<RpcType> = listOf(),
    val inlinedType: RpcType? = null
) {

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




const val GeneratedModelsPackage = "io.github.natanfudge.generated.models"

/**
 * Only serializes the name of the [KotlinTypeReference], so just a string
 */
class KotlinTypeReferenceNameSerializer : KSerializer<KotlinTypeReference> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): KotlinTypeReference {
        return KotlinTypeReference(packageName = GeneratedModelsPackage, simpleName = decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: KotlinTypeReference) {
        encoder.encodeString(value.simpleName)
    }

}

/**
 * [KotlinTypeReference] is serialized to a [RpcType]
 */
class KotlinTypeReferenceSerializer : KSerializer<KotlinTypeReference> {
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
            RpcType(name, isNullable = isNullable)
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
            val typeArgument = RpcType(typeArgumentName)
            RpcType(RpcType.Array, isNullable = isNullable, typeArguments = listOf(typeArgument))
        }

        "Pair", "Triple", "Array" -> {
            // Types that use normal type arguments - pair, triple, array
            val name = if (simpleName == "Array") RpcType.Array else RpcType.Tuple
            RpcType(
                name = name,
                isNullable = isNullable,
                typeArguments = typeArguments.map { it.toRpcType() },
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
        "Map.Entry" -> RpcType.Tuple
        else -> error(
            "Unexpected kotlin builtin collection type: ${className}." +
                    " Was a custom class declared in kotlin.collections.*? This is probably a bug in RPC4K."
        )
    }

    return RpcType(
        name = name,
        isNullable = isNullable,
        typeArguments = typeArguments.map { it.toRpcType() },
    )
}


private fun KotlinTypeReference.toUserType(): RpcType {
    return RpcType(
        // We don't have nested classes in RPC4all
        name = simpleName.substringAfterLast("."),
        isNullable = isNullable,
        isTypeParameter = isTypeParameter,
        typeArguments = typeArguments.map { it.toRpcType() },
        inlinedType = inlinedType?.toRpcType()
    )
}

//TODO: This implementation doesn't support default values in parameters.
// There's no easy to way to tell kotlin "use the default value if this value is null".
// Kotlin serialization can only do it because it's a compiler plugin that can use the underlying full-args constructor that accepts possibly null/0 values.
// If we want to support this in a really native way of `x: Int = 2` then we probably need a compiler plugin.
// It's not even possible to copy the initializer code ourselves because ksp doesn't provide code.