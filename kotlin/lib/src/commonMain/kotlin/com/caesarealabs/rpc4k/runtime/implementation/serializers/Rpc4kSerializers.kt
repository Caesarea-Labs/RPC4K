package com.caesarealabs.rpc4k.runtime.implementation.serializers

import com.benasher44.uuid.Uuid
import com.caesarealabs.rpc4k.runtime.implementation.KotlinClassName
import com.caesarealabs.rpc4k.runtime.implementation.KotlinMethodName
import com.caesarealabs.rpc4k.runtime.implementation.KotlinName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

// Used for the processor only
public val Rpc4kSerializers: List<Rpc4kSerializer<*>> = Rpc4kSerializersModuleBuilder().apply {
    obj(VoidUnitSerializer, "kotlin", "Unit", "com.caesarealabs.rpc4k.runtime.implementation.serializers", "VoidUnitSerializer")
    obj(UUIDSerializer, "com.benasher44.uuid","Uuid","com.caesarealabs.rpc4k.runtime.implementation.serializers", "UUIDSerializer")
    // Use our serializer for the java UUID as because the MP UUID is typealiased to it
    obj(UUIDSerializer, "java.util","UUID","com.caesarealabs.rpc4k.runtime.implementation.serializers", "UUIDSerializer")
//    obj(InstantIsoSerializer)
//    obj(ZonedDateTimeIsoSerializer)
    builtinSerializerMethod(Pair::class, "kotlin", "Pair", "TuplePairSerializer") {
        TuplePairSerializer(it[0], it[1])
    }
    builtinSerializerMethod(Triple::class, "kotlin", "Triple", "TupleTripleSerializer") {
        TupleTripleSerializer(it[0], it[1], it[2])
    }
    builtinSerializerMethod(Map.Entry::class, "kotlin.collections", "Map.Entry", "TupleMapEntrySerializer") {
        TupleMapEntrySerializer(it[0], it[1])
    }
}.build()

@Suppress("UNCHECKED_CAST")
public val Rpc4kSerializersModule: SerializersModule = SerializersModule {
    for (serializer in Rpc4kSerializers) {
        when (val provider = serializer.provider) {
            is ContextualProvider.Argless -> contextual(serializer.serializedKClass as KClass<Any>, provider.serializer as KSerializer<Any>)
            is ContextualProvider.WithTypeArguments -> contextual(serializer.serializedKClass, provider.provider)
        }
    }
}

// This could become a public api in the future
private class Rpc4kSerializersModuleBuilder {
    @PublishedApi
    internal val serializers = mutableListOf<Rpc4kSerializer<*>>()

    /**
     * Class names are passed explicitly for type aliases to work
     */
    fun <T : Any> method(
        kClass: KClass<T>,
        typePackage: String,
        typeName: String,
        serializerName: String,
        serializerPackage: String,
        provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>
    ) {
        serializers.add(
            Rpc4kSerializer.Function(
                KotlinMethodName(serializerName, serializerPackage), ContextualProvider.WithTypeArguments(provider), kClass,
                KotlinClassName(pkg = typePackage, simple = typeName)
            )
        )
    }

    /**
     * Class names are passed explicitly for type aliases to work
     */
    inline fun <reified T : Any> obj(
        instance: KSerializer<T>,
        typePackage: String,
        typeName: String,
        serializerPackage: String,
        serializerName: String
    ) {
        serializers.add(
            Rpc4kSerializer.Object(
                KotlinClassName(simple = serializerName, pkg = serializerPackage),
                ContextualProvider.Argless(instance),
                T::class,
                KotlinClassName(simple = typeName, pkg = typePackage)
            )
        )
    }

//    @PublishedApi
//    internal fun <T : Any> add(qualifiedName: KotlinName, provider: ContextualProvider, kind: Rpc4kSerializer.Kind, kClass: KClass<T>) {
//        serializers.add(Rpc4kSerializer(qualifiedName, provider, kind, kClass))
//    }
//
//    @PublishedApi
//    internal inline fun <reified T : Any> add(name: KotlinName, provider: ContextualProvider, kind: Rpc4kSerializer.Kind) {
//        add(name, provider, kind, T::class)
//    }

//    @PublishedApi
//    internal inline fun <reified T : Any> method(name: KotlinName, provider: ContextualProvider) {
//        add<T>(name, provider, Rpc4kSerializer.Kind.Method)
//    }

    internal fun build(): List<Rpc4kSerializer<*>> {
        return serializers
    }
}

private fun <T : Any> Rpc4kSerializersModuleBuilder.builtinSerializerMethod(
    clazz: KClass<T>,
    typePackage: String,
    typeName: String,
    name: String, provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>
) = method(
    clazz,
    serializerName = name,
    serializerPackage = "com.caesarealabs.rpc4k.runtime.implementation.serializers",
    typeName = typeName,
    typePackage = typePackage,
    provider = provider
)


public sealed interface Rpc4kSerializer<T : Any> {

    public val serializerName: KotlinName
    public val provider: ContextualProvider
    public val serializedKClass: KClass<T>

    // KClass is not good enough for type aliases
    public val serializedName: KotlinClassName

    public data class Function<T : Any>(
        override val serializerName: KotlinMethodName,
        override val provider: ContextualProvider,
        override val serializedKClass: KClass<T>,
        override val serializedName: KotlinClassName,
    ) : Rpc4kSerializer<T>

    public data class Object<T : Any>(
        override val serializerName: KotlinClassName,
        override val provider: ContextualProvider.Argless,
        override val serializedKClass: KClass<T>,
        override val serializedName: KotlinClassName,
    ) : Rpc4kSerializer<T>
}

public sealed interface ContextualProvider {
    public class Argless(internal val serializer: KSerializer<*>) : ContextualProvider
    public class WithTypeArguments(internal val provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>) : ContextualProvider
}
