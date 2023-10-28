package io.github.natanfudge.rpc4k.runtime.implementation.serializers

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import io.github.natanfudge.rpc4k.runtime.implementation.KotlinClassName
import io.github.natanfudge.rpc4k.runtime.implementation.KotlinMethodName
import io.github.natanfudge.rpc4k.runtime.implementation.KotlinName
import io.github.natanfudge.rpc4k.runtime.implementation.kotlinName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass


//TODO: is a class -> serializer map the right structure? need to see if we are using that property in the end of the day
internal val Rpc4kSerializers: List<Rpc4kSerializer<*>> = Rpc4kSerializersModuleBuilder().apply {
    obj(VoidUnitSerializer)
    obj(UUIDSerializer)
    obj(InstantIsoSerializer)
    obj(ZonedDateTimeIsoSerializer)
    builtinSerializerMethod(Pair::class, "TuplePairSerializer") {
        TuplePairSerializer(it[0], it[1])
    }
    builtinSerializerMethod(Triple::class, "TupleTripleSerializer") {
        TupleTripleSerializer(it[0], it[1], it[2])
    }
    builtinSerializerMethod(Map.Entry::class,"TupleMapEntrySerializer") {
        TupleMapEntrySerializer(it[0], it[1])
    }
}.build()

@Suppress("UNCHECKED_CAST")
val Rpc4kSerializersModule = SerializersModule {
    for (serializer in Rpc4kSerializers) {
        when (val provider = serializer.provider) {
            is ContextualProvider.Argless -> contextual(serializer.kClass as KClass<Any>, provider.serializer as KSerializer<Any>)
            is ContextualProvider.WithTypeArguments -> contextual(serializer.kClass, provider.provider)
        }
    }
}

// This could become a public api in the future
class Rpc4kSerializersModuleBuilder {
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

    inline fun <reified T : Any, reified O : KSerializer<T>> obj(instance: O) {
        serializers.add(Rpc4kSerializer.Object(O::class.kotlinName, ContextualProvider.Argless(instance), T::class))
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

private  fun < T : Any> Rpc4kSerializersModuleBuilder.builtinSerializerMethod(
    clazz: KClass<T>,
    name: String,  provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>
) = method(clazz, name, "io.github.natanfudge.rpc4k.runtime.implementation.serializers", provider)



@PublishedApi
internal sealed interface Rpc4kSerializer<T : Any> {

    val name: KotlinName
    val provider: ContextualProvider
    val kClass: KClass<T>

    data class Function<T : Any>(
        override val name: KotlinMethodName,
        override val provider: ContextualProvider,
        override val kClass: KClass<T>
    ) : Rpc4kSerializer<T>

    data class Object<T : Any>(
        override val name: KotlinClassName,
        override val provider: ContextualProvider.Argless,
        override val kClass: KClass<T>
    ) : Rpc4kSerializer<T>
}

@PublishedApi
internal sealed interface ContextualProvider {
    class Argless(val serializer: KSerializer<*>) : ContextualProvider
    class WithTypeArguments(val provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>) : ContextualProvider
}
