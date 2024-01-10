package com.caesarealabs.rpc4k.processor

import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.caesarealabs.rpc4k.processor.utils.appendIf
import com.caesarealabs.rpc4k.runtime.api.Dispatch
import com.caesarealabs.rpc4k.runtime.implementation.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
internal data class RpcApi(
    /**
     * We need the full name in kotlin , and only want the simple name in other languages accessing the generated file.
     *
     * Note: maybe this means i should have a different model for the generated stuff?
     */
    @Serializable(SimpleNameOnlyKotlinNameSerializer::class) val name: KotlinClassName,
    val methods: List<RpcFunction>,
    val events: List<RpcEventEndpoint>,
    val models: List<RpcModel>
)

internal sealed interface RpcEndpoint {
    val name: String
//    val parameters: List<RpcParameter>
    val returnType: KotlinTypeReference
}

@Serializable
internal data class RpcFunction(override val name: String,  val parameters: List<RpcParameter>,override  val returnType: KotlinTypeReference): RpcEndpoint

@Serializable
internal data class EventParameter(
    /**
     * Whether this parameter is annotated with [Dispatch]
     */
    val isDispatch: Boolean,
    val value: RpcParameter
)

@Serializable internal data class RpcEventEndpoint(
    override val name: String,  val parameters: List<EventParameter>,override  val returnType: KotlinTypeReference
): RpcEndpoint {
//    override val parameters: List<RpcParameter> = eventParameters.map { it.value }
}

@Serializable
//NiceToHave: support optional parameters
internal data class RpcParameter(val name: String, val type: KotlinTypeReference, /*val isOptional: Boolean*//* = false*/)

@Serializable
internal sealed interface RpcModel {
    val name: String
    val typeParameters: List<String>

    @Serializable
    @SerialName("inline")
    data class Inline(override val name: String, override val typeParameters: List<String> = listOf(), val inlinedType: KotlinTypeReference) :
        RpcModel


    @Serializable
    @SerialName("struct")
    data class Struct(
        val hasTypeDiscriminator: Boolean,
        override val name: String,
        // BLOCKED: Get rid of this, i don't want it in the final spec
        val packageName: String,
        override val typeParameters: List<String> = listOf(),
        val properties: List<Property>,
    ) : RpcModel {
        /**
         * @param isOptional BLOCKED: Support optional parameters and properties
         */
        @Serializable

        data class Property(val name: String, val type: KotlinTypeReference,/* val isOptional: Boolean*//* = false*/)
    }

    @Serializable
    @SerialName("enum")
    data class Enum(override val name: String, val options: List<String>) : RpcModel {
        override val typeParameters: List<String> = listOf()
    }

    /**
     * Important note: Languages implementing this MUST add a `type: ` field to the structs referenced by `options` so the other side
     * may know which of the union options a union value is
     * @param [options] a list of possible types this union can evaluate to.
     */
    @Serializable
    @SerialName("union")
    data class Union(override val name: String, val options: List<KotlinTypeReference>, override val typeParameters: List<String> = listOf()) :
        RpcModel
}


/**
 * More precise description of an [RpcType] that comes from the JVM, and makes it easier to generate kotlin code as it includes the package name of the type
 * and uses kotlin class names and not RPC class names
 */
@Serializable(with = KotlinTypeReferenceSerializer::class)
internal data class KotlinTypeReference(
    val name: KotlinClassName,
//    val packageName: String,
//    val simpleName: String,
    val isNullable: Boolean = false,
    // True in cases where the value is initialized by a default value
    val hasDefaultValue: Boolean = false,
    val typeArguments: List<KotlinTypeReference> = listOf(),
    val isTypeParameter: Boolean = false,
    val inlinedType: KotlinTypeReference? = null
) {
    companion object {
        val string = KotlinTypeReference(KotlinClassName("kotlin", "String"))
    }

    // Inner classes are dot seperated
//    val qualifiedName = "$packageName.$simpleName"

    val rawTypeName = name.kotlinPoet

    val typeName: TypeName = rawTypeName.let { name ->
        if (typeArguments.isEmpty()) name else name.parameterizedBy(typeArguments.map { it.typeName })
    }.copy(nullable = isNullable)
    val isUnit get() = name.isUnit
}


// BLOCKED: get rid of packageName here
@Serializable
internal data class RpcType(
    val name: String,
    val isTypeParameter: Boolean = false,
    val isNullable: Boolean = false,
    val typeArguments: List<RpcType> = listOf(),
    val inlinedType: RpcType? = null
) {

    init {
        // Kotlin doesn't have higher-kinded types yet
        if (isTypeParameter) check(typeArguments.isEmpty()
        ) { "It doesn't make sense for type parameter <$name> to have type parameters: <${typeArguments.joinToString()}>" }
        if (isTypeParameter) check(inlinedType == null) { "It doesn't make sense for type parameter <$name> to be an inlined type: $inlinedType" }
    }

    override fun toString(): String {
        val string = if (isTypeParameter) {
            name
        } else {
            name
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
        const val UUID = "uuid"
        const val F32 = "f32"
        const val F64 = "f64"
        const val Char = "char"
        const val String = "string"
        const val Void = "void"
        const val Array = "array"
        const val Record = "record"
        const val Tuple = "tuple"
        const val Date = "date"
        const val Duration = "duration"
    }
}


internal const val GeneratedModelsPackage = "${GeneratedCodeUtils.Package}.models"


/**
 * [KotlinTypeReference] is serialized to a [RpcType]
 */
internal class KotlinTypeReferenceSerializer : KSerializer<KotlinTypeReference> {
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


private fun KotlinTypeReference.toRpcType() = when (name.pkg) {
    "kotlin" -> toBuiltinRpcType()
    "kotlin.collections" -> toBuiltinCollectionType()
    "kotlin.time" -> toDurationType()
    "java.time" -> toDateType()
    "java.util" -> toUUID()
    else -> toUserType()
}



/**
 * Converts Kotlin types like Int to RPC types like i32.
 */
private fun KotlinTypeReference.toBuiltinRpcType(): RpcType {
    return when (name.simple) {
        "Boolean", "Byte", "UByte", "Short", "UShort", "Int", "UInt", "Long", "ULong", "Char", "String", "Unit", "Float", "Double" -> {
            // Primitive types
            // Unsigned types are treated the same as the normal types
            val name = when (name.simple) {
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
                else -> error("Impossible class name ${name.simple}")
            }
            buildRpcType(name = name)
        }

        "ByteArray", "ShortArray", "IntArray", "LongArray", "CharArray", "UByteArray", "UShortArray", "UIntArray", "ULongArray" -> {
            // Primitive array types
            val typeArgumentName = when (name.simple) {
                // We expand XArray into an array of the respective type X
                // Unsigned types are treated the same as the normal types
                "ByteArray", "UByteArray" -> RpcType.I8
                "ShortArray", "UShortArray" -> RpcType.I16
                "IntArray", "UIntArray" -> RpcType.I32
                "LongArray", "ULongArray" -> RpcType.I64
                "CharArray" -> RpcType.Char
                else -> error("Impossible class name $rawTypeName")
            }
            val typeArgument = RpcType(name = typeArgumentName)
            buildRpcType(name = RpcType.Array, typeArguments = listOf(typeArgument))
        }

        "Pair", "Triple", "Array" -> {
            // Types that use normal type arguments - pair, triple, array
            val name = if (name.simple == "Array") RpcType.Array else RpcType.Tuple
            buildRpcType(name = name)
        }

        else -> error(
            "Unexpected kotlin builtin type: ${rawTypeName}." +
                " Was a custom class declared in kotlin.*? This is probably a bug in RPC4K."
        )
    }
}

private fun KotlinTypeReference.toBuiltinCollectionType(): RpcType {
    val name = when (name.simple) {
        "List", "Set", "MutableList", "MutableSet" -> RpcType.Array
        "Map", "MutableMap" -> RpcType.Record
        "Map.Entry" -> RpcType.Tuple
        else -> error(
            "Unexpected kotlin builtin collection type: ${rawTypeName}." +
                " Was a custom class declared in kotlin.collections.*? This is probably a bug in RPC4K."
        )
    }

    return buildRpcType(name = name)
}


private fun KotlinTypeReference.buildRpcType(
    name: String,
    isNullable: Boolean = this.isNullable,
    typeArguments: List<RpcType> = this.typeArguments.map { it.toRpcType() }
): RpcType {
    return RpcType(name = name, isNullable = isNullable, typeArguments = typeArguments)
}

private fun KotlinTypeReference.toDateType(): RpcType {
    if (name.simple == "ZonedDateTime" || name.simple == "Instant") {
        return buildRpcType(name = RpcType.Date, typeArguments = listOf())
    } else {
        error("Unexpected kotlin date type: ${name.simple}. These shouldn't be accepted by the compiler.")
    }
}

private fun KotlinTypeReference.toUUID(): RpcType {
    if (name.simple == "UUID") {
        return buildRpcType(name = RpcType.UUID, typeArguments = listOf())
    } else {
        error("Unexpected kotlin java.util type: ${name.simple}. These shouldn't be accepted by the compiler.")
    }
}

private fun KotlinTypeReference.toDurationType(): RpcType {
    if (name.simple == "Duration") {
        return buildRpcType(name = RpcType.Duration, typeArguments = listOf())
    } else {
        error("Unexpected kotlin kotlin.time type: ${name.simple}. These shouldn't be accepted by the compiler.")
    }
}


private fun KotlinTypeReference.toUserType(): RpcType {
    return RpcType(
        name = name.simple,
        isNullable = isNullable,
        isTypeParameter = isTypeParameter,
        typeArguments = typeArguments.map { it.toRpcType() },
        inlinedType = inlinedType?.toRpcType()
    )
}

