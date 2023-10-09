package io.github.natanfudge.rpc4k.processor.utils

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.natanfudge.rpc4k.processor.RpcType
import io.github.natanfudge.rpc4k.processor.utils.poet.*
import io.github.natanfudge.rpc4k.processor.utils.poet.FormattedString
import io.github.natanfudge.rpc4k.processor.utils.poet.formatWith
import io.github.natanfudge.rpc4k.processor.utils.poet.withArgumentList
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

private fun KSType.isBuiltinSerializableType() =
    declaration.qualifiedName?.asString() in builtinSerializableTypes

internal fun RpcType.toSerializerString() = when (this) {
    is RpcType.Ksp -> value.resolve().toSerializerString()
}

private fun KSType.toSerializerString(): FormattedString {
    val name = nonNullQualifiedName()

    return if (name in builtinSerializableTypes) {
        if (nonNullQualifiedName() in classesWithSeparateSerializerMethod) {
            // Serializers like MapSerializer
            topLevelSerializerMethod()
        } else {
            builtinExtensionSerializerMethod()
        }
    } else {
        userClassSerializer()
    }
}
/**
 * Gets serializers like ListSerializer, SetSerializer, etc
 */
private fun KSType.topLevelSerializerMethod(): FormattedString {
    // Map.Entry needs special handling to get the correct serializer
    val name = declaration.simpleName.asString().let { if(it == "Entry") "MapEntry" else it }
    // For example, for the name Map.Entry we get MapEntrySerializer
    val serializerMethod = MemberName("kotlinx.serialization.builtins", "${name}Serializer")
    return serializerMethod.withSerializerArguments(arguments)
}

private fun KSType.builtinExtensionSerializerMethod(): FormattedString {
    return "%T.serializer()".formatWith(toTypeName())
}

/**
 * These classes don't have a T.serializer() for some reason but instead have a separate top-level method
 */
private val classesWithSeparateSerializerMethod = listOf(
    List::class, Set::class, Map::class, Pair::class, Map.Entry::class, Triple::class
).map { it.qualifiedName }.toHashSet()


private fun KSType.userClassSerializer() = "%T.serializer".formatWith(toClassName())
    .withMethodSerializerArguments(arguments)

private fun FormattedString.withMethodSerializerArguments(args: List<KSTypeArgument>) = withMethodArguments(
    args.map { it.nonNullType().resolve().toSerializerString() }
)

private fun MemberName.withSerializerArguments(args: List<KSTypeArgument>) = withArgumentList(
    args.map { it.nonNullType().resolve().toSerializerString() }
)


