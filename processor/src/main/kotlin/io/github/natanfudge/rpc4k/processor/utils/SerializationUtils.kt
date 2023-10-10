package io.github.natanfudge.rpc4k.processor.utils

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.natanfudge.rpc4k.processor.RpcType
import io.github.natanfudge.rpc4k.processor.utils.poet.FormattedString
import io.github.natanfudge.rpc4k.processor.utils.poet.formatWith
import io.github.natanfudge.rpc4k.processor.utils.poet.withArgumentList
import io.github.natanfudge.rpc4k.processor.utils.poet.withMethodArguments
import kotlinx.serialization.Serializable

internal fun KSType.isSerializable() = isBuiltinSerializableType() || isAnnotatedBySerializable()


private val serializableClassName = Serializable::class.qualifiedName

private fun KSType.isAnnotatedBySerializable() =
    declaration.annotations.any { it.annotationType.resolve().toClassName().canonicalName == serializableClassName }

private val builtinSerializableTypes = listOf(
    Byte::class,
    Short::class,
    Int::class,
    Long::class,
    Float::class,
    Double::class,
    Boolean::class,
    Unit::class,
    String::class,
    Char::class,
    UInt::class,
    ULong::class,
    UByte::class,
    UShort::class,
    Set::class,
    List::class,
    Pair::class,
    Triple::class,
    Map.Entry::class,
    Map::class,
).map { it.qualifiedName!! }.toHashSet()

private fun KSType.isBuiltinSerializableType() = declaration.qualifiedName?.asString() in builtinSerializableTypes


internal fun RpcType.toSerializerString(): FormattedString {
    val withoutNullable = if (qualifiedName in builtinSerializableTypes) {
        if (qualifiedName in classesWithSeparateSerializerMethod) {
            // Serializers like MapSerializer
            topLevelSerializerMethod()
        } else {
            builtinExtensionSerializerMethod()
        }
    } else {
        userClassSerializer()
    }
    // Add .nullable if needed
    return if (isNullable) {
        withoutNullable + ".nullable"
    } else {
        withoutNullable
    }
}

/**
 * Gets serializers like ListSerializer, SetSerializer, etc
 */
private fun RpcType.topLevelSerializerMethod(): FormattedString {
    // Map.Entry needs special handling to get the correct serializer
    val name = simpleName.let { if (it == "Map.Entry") "MapEntry" else it }
    // For example, for the name Map.Entry we get MapEntrySerializer
    val serializerMethod = MemberName("kotlinx.serialization.builtins", "${name}Serializer")
    return serializerMethod.withSerializerArguments(typeArguments)
}

private fun RpcType.builtinExtensionSerializerMethod(): FormattedString {
    return "%T.serializer()".formatWith(className)
}

/**
 * These classes don't have a T.serializer() for some reason but instead have a separate top-level method
 */
private val classesWithSeparateSerializerMethod = listOf(
    List::class, Set::class, Map::class, Pair::class, Map.Entry::class, Triple::class
).map { it.qualifiedName }.toHashSet()


// Nullability is handled separately
private fun RpcType.userClassSerializer() = "%T.serializer".formatWith(className)
    .withMethodSerializerArguments(typeArguments)

private fun FormattedString.withMethodSerializerArguments(args: List<RpcType>) = withMethodArguments(
    args.map { it.toSerializerString() }
)

private fun MemberName.withSerializerArguments(args: List<RpcType>) = withArgumentList(
    args.map { it.toSerializerString() }
)


