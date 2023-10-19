package io.github.natanfudge.rpc4k.runtime.implementation

import io.github.natanfudge.rpc4k.runtime.api.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * These functions are used by generated code and code that interacts with them
 */
object GeneratedCodeUtils {
    const val FactoryName = "Factory"
    const val ClientSuffix = "ClientImpl"
    const val ServerSuffix = "ServerImpl"
    const val Package = "io.github.natanfudge.rpc4k.generated"


    /**
     * Sends a value and returns the result
     */
    suspend fun <T> request(
        client: RpcClient,
        format: SerializationFormat,
        methodName: String,
        args: List<*>,
        argSerializers: List<KSerializer<*>>,
        responseSerializer: KSerializer<T>
    ): T {
        val rpc = Rpc(methodName, args)
        val result = client.send(rpc, format, argSerializers)
        return format.decode(responseSerializer, result)
    }

    /**
     * Sends a value, not caring about the result
     */
    suspend fun send(client: RpcClient, format: SerializationFormat, methodName: String, args: List<Any?>, argSerializers: List<KSerializer<*>>) {
        val rpc = Rpc(methodName, args)
        client.send(rpc, format, argSerializers)
    }



    /**
     * Catches rpc exceptions and sends the correct error back to the client
     */
    suspend inline fun withCatching(server: RpcServer, handler: () -> Unit) {
        try {
            handler()
        } catch (e: RpcServerException) {
            Rpc4K.Logger.warn("Invalid request", e)
            // RpcServerException messages are trustworthy
            server.sendError(e.message, RpcError.InvalidRequest)
        } catch (e: Throwable) {
            Rpc4K.Logger.error("Failed to handle request", e)
            // Don't send arbitrary throwable messages because it could leak data
            server.sendError("Server failed to process request", RpcError.InternalError)
        }
    }

    /**
     * Uses the [server] to respond with the specified data
     */
    suspend fun <T> respond(
        format: SerializationFormat,
        server: RpcServer,
        request: ByteArray,
        argDeserializers: List<KSerializer<*>>,
        resultSerializer: KSerializer<T>,
        respondMethod: suspend (args: List<*>) -> T
    ) {
        val parsed = try {
            Rpc.fromByteArray(request, format, argDeserializers)
        } catch (e: SerializationException) {
            throw RpcServerException("Malformed request arguments: ${e.message}", e)
        }
        val result = respondMethod(parsed.arguments)
        server.send(format, result, resultSerializer)
    }
}


///**
// * Creates a format with a map from each sealed class, to the serializer of each discriminator of it subtypes.
// * Wraps the serializers of the selected serializer classes to make the serial name use the simple name instead of the qualified name
// */
//@Suppress("UNCHECKED_CAST")
//fun <T : SerializationFormat> SerializationFormatProvider<T>.wrapSerializers(serializerMap: Map<KClass<*>, Map<KClass<*>, KSerializer<*>>>): T {
//    return provide(SerializersModule {
//        for ((sealedClass, subclassSerializer) in serializerMap) {
//            polymorphic(sealedClass as KClass<Any>) {
//                for((subclass, serializer) in subclassSerializer) {
//                    subclass(subclass as KClass<Any>, SimplifiedSerialNameSerializer(serializer as KSerializer<Any>))
//                }
//            }
//        }
//    })
//}

//fun main() {
//    val jsonString = "{\"type\":\"Bar\"}"
//    val json = Json {
//        SerializersModule {
//            polymorphic(Foo::class) {
//                subclass(Foo.Bar::class, (Foo.Bar.serializer().simpleSerialName)
//                subclass(Foo.Bix::class, Foo.Bix.serializer().simpleSerialName)
//            }
//        }
//    }
//
//    val back = json.decodeFromString<Foo>(SealedClassSerializer("Foo", Foo) jsonString)
//    println(back)
//}
//
//@Serializable
//sealed interface Foo {
//    @Serializable
//    class Bar: Foo
//    @Serializable
//    class Bix: Foo
//}
//
////TODO: do i need to use this on generated sealed classes?
//val <T> KSerializer<T>.simpleSerialName get() = SimplifiedSerialNameSerializer(this)
//
///**
// * Wraps [serializer] in a way that its serialName will be the simple name instead of the qualified name
// * This is necessary because the rpc4all spec doesn't have the concept of package names.
// */
//class SimplifiedSerialNameSerializer<T>(private val serializer: KSerializer<T>) : KSerializer<T> by serializer {
//    override val descriptor: SerialDescriptor = SimplifiedSerialNameDescriptor(serializer.descriptor)
//}
//
//class SimplifiedSerialNameDescriptor(private val descriptor: SerialDescriptor) : SerialDescriptor by descriptor {
//    @OptIn(ExperimentalSerializationApi::class)
//    override val serialName: String = descriptor.serialName.substringAfterLast(".")
//}

//TODO: Seems like kotlin serialization doesn't give a shit what we have in the contextual serialization for the top-level object. /
// The only way to specify a different serializer is by doing it explicitly, at compile time. Therefore, I should add an api for registering
// custom serializers at compile time, and potentially use that api in the implementation as well.
// Something like this:
// @CustomSerializer(SomeClass::class, SomeSerializer::class)
// @Service
// class MyClass { ... }
