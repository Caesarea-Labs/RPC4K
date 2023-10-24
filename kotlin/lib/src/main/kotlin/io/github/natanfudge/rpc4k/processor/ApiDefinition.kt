package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import io.github.natanfudge.rpc4k.processor.utils.appendIf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


/**
 * @param name The type of this is not exactly accurate, as it's always not nullable and with no type arguments, so we serialize it to just a String.
 */
@Serializable
data class ApiDefinition(
    @Serializable(with = KotlinTypeReferenceNameSerializer::class) val name: KotlinTypeReference,
    val methods: List<RpcDefinition>,
    val models: List<RpcModel>
)

/**
 * the [keepo 123][name]
 */
@Serializable
data class RpcDefinition(val name: String, val parameters: List<RpcParameter>, val returnType: KotlinTypeReference)

@Serializable
data class RpcParameter(val name: String, val type: KotlinTypeReference)

@Serializable
sealed interface RpcModel {
    val name: String


    @Serializable
    @SerialName("struct")
    data class Struct(
        override val name: String, val typeParameters: List<String> = listOf(), val properties: List<Property>,
    ) : RpcModel {
        /**
         * @param isOptional BLOCKED: Support optional parameters and properties
         */
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
    val packageName: String,
    val isTypeParameter: Boolean = false,
    val isNullable: Boolean = false,
    val typeArguments: List<RpcType> = listOf(),
    val inlinedType: RpcType? = null
) {

    init {
        // Kotlin doesn't have higher-kinded types yet
        if (isTypeParameter) check(typeArguments.isEmpty()) { "It doesn't make sense for type parameter <$name> to have type parameters: <${typeArguments.joinToString()}>" }
        if (isTypeParameter) check(inlinedType == null) { "It doesn't make sense for type parameter <$name> to be an inlined type: $inlinedType" }
    }

    override fun toString(): String {
        val string = if (isTypeParameter) {
            name
        } else {
            "$packageName.$name"
                .appendIf(typeArguments.isNotEmpty()) { "<${typeArguments.joinToString()}>" }
                .appendIf(inlinedType != null) { "(Inlining $inlinedType)" }
        }
        return string.appendIf(isNullable) { "?" }
    }

    companion object BuiltinNames {
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

private fun RpcType.toKotlinTypeReference(packageName: String): KotlinTypeReference {
    // NiceToHave: Generate Kotlin clients from non-kotlin servers
    throw UnsupportedOperationException("Generate Kotlin clients from non-kotlin servers")
}

private fun KotlinTypeReference.toRpcType() = when (packageName) {
    "kotlin" -> toBuiltinRpcType()
    "kotlin.collections" -> toBuiltinCollectionType()
    "java.time" -> toDateType()
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
            buildRpcType(name = name)
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
            val typeArgument = RpcType(name = typeArgumentName, packageName = packageName)
            buildRpcType(name = RpcType.Array, typeArguments = listOf(typeArgument))
        }

        "Pair", "Triple", "Array" -> {
            // Types that use normal type arguments - pair, triple, array
            val name = if (simpleName == "Array") RpcType.Array else RpcType.Tuple
            buildRpcType(name = name)
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

    return buildRpcType(name = name)
}


private fun KotlinTypeReference.buildRpcType(
    name: String,
    isNullable: Boolean = this.isNullable,
    typeArguments: List<RpcType> = this.typeArguments.map { it.toRpcType() },
    packageName: String = this.packageName
): RpcType {
    return RpcType(name = name,packageName = packageName,isNullable = isNullable, typeArguments = typeArguments)
}

private fun KotlinTypeReference.toDateType(): RpcType {
    if (simpleName == "ZonedDateTime" || simpleName == "Instant") {
        return buildRpcType(name = "date", typeArguments = listOf())
    } else {
        error("Unexpected kotlin date type: ${simpleName}. These shouldn't be accepted by the compiler.")
    }
}


private fun KotlinTypeReference.toUserType(): RpcType {
    return RpcType(
        name = simpleName,
        packageName = packageName,
        isNullable = isNullable,
        isTypeParameter = isTypeParameter,
        typeArguments = typeArguments.map { it.toRpcType() },
        inlinedType = inlinedType?.toRpcType()
    )
}

