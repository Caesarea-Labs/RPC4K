package com.caesarealabs.rpc4k.runtime.implementation.serializers

import com.caesarealabs.rpc4k.runtime.implementation.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

// Used for the processor only
public val Rpc4kSerializers: List<Rpc4kSerializer<*>> = Rpc4kSerializersModuleBuilder().apply {
    obj(VoidUnitSerializer, "com.caesarealabs.rpc4k.runtime.implementation.serializers", "VoidUnitSerializer")
//    obj(UUIDSerializer)
//    obj(InstantIsoSerializer)
//    obj(ZonedDateTimeIsoSerializer)
    builtinSerializerMethod(Pair::class, "TuplePairSerializer") {
        TuplePairSerializer(it[0], it[1])
    }
    builtinSerializerMethod(Triple::class, "TupleTripleSerializer") {
        TupleTripleSerializer(it[0], it[1], it[2])
    }
    builtinSerializerMethod(Map.Entry::class, "TupleMapEntrySerializer") {
        TupleMapEntrySerializer(it[0], it[1])
    }
}.build()

@Suppress("UNCHECKED_CAST")
public val Rpc4kSerializersModule: SerializersModule = SerializersModule {
    for (serializer in Rpc4kSerializers) {
        when (val provider = serializer.provider) {
            is ContextualProvider.Argless -> contextual(serializer.kClass as KClass<Any>, provider.serializer as KSerializer<Any>)
            is ContextualProvider.WithTypeArguments -> contextual(serializer.kClass, provider.provider)
        }
    }
}

// This could become a public api in the future
private class Rpc4kSerializersModuleBuilder {
    @PublishedApi
    internal val serializers = mutableListOf<Rpc4kSerializer<*>>()

    fun <T : Any> method(
        kClass: KClass<T>,
        methodName: String,
        packageName: String,
        provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>
    ) {
        serializers.add(Rpc4kSerializer.Function(KotlinMethodName(methodName, packageName), ContextualProvider.WithTypeArguments(provider), kClass))
    }

    inline fun <reified T : Any, O : KSerializer<T>> obj(instance: O, packageName: String, className: String) {
        serializers.add(Rpc4kSerializer.Object(KotlinClassName(simple = className, pkg = packageName), ContextualProvider.Argless(instance), T::class))
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
    name: String, provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>
) = method(clazz, name, "com.caesarealabs.rpc4k.runtime.implementation.serializers", provider)


public sealed interface Rpc4kSerializer<T : Any> {

    public val name: KotlinName
    public val provider: ContextualProvider
    public val kClass: KClass<T>

    public data class Function<T : Any>(
        override val name: KotlinMethodName,
        override val provider: ContextualProvider,
        override val kClass: KClass<T>
    ) : Rpc4kSerializer<T>

    public data class Object<T : Any>(
        override val name: KotlinClassName,
        override val provider: ContextualProvider.Argless,
        override val kClass: KClass<T>
    ) : Rpc4kSerializer<T>
}

public sealed interface ContextualProvider {
    public class Argless(internal val serializer: KSerializer<*>) : ContextualProvider
    public class WithTypeArguments(internal val provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>) : ContextualProvider
}
