package com.caesarealabs.rpc4k.runtime.implementation

import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.implementation.serializers.TupleSerializer
import kotlinx.serialization.*

/**
 * These functions are used by generated code and code that interacts with them
 */
public object GeneratedCodeUtils {
    @PublishedApi
    internal const val FactoryName: String = "Factory"

    @PublishedApi
    internal const val ClientSuffix: String = "ClientImpl"

    @PublishedApi
    internal const val ServerSuffix: String = "ServerImpl"

    @PublishedApi
    internal const val Group: String = "com.caesarealabs"

    @PublishedApi
    internal const val Package: String = "${Group}.rpc4k.generated"


    /**
     * Sends a value and returns the result
     */
    public suspend fun <T> request(
        client: RpcClient,
        format: SerializationFormat,
        methodName: String,
        args: List<Any?>,
        argSerializers: List<KSerializer<*>>,
        responseSerializer: KSerializer<*>
    ): T {
        val rpc = Rpc(methodName, args)
        val result = client.send(rpc, format, argSerializers)
        return format.decode(responseSerializer, result) as T
    }

    /**
     * Sends a value, not caring about the result
     */
    public suspend fun send(
        client: RpcClient, format: SerializationFormat, methodName: String, args: List<Any?>,
        argSerializers: List<KSerializer<*>>
    ) {
        val rpc = Rpc(methodName, args)
        client.send(rpc, format, argSerializers)
    }

    /**
     * Uses the [server] to respond with the specified data
     */
    public suspend fun <T> respond(
        setup: AnyRpcServerSetup,
        request: ByteArray,
        argDeserializers: List<KSerializer<*>>,
        resultSerializer: KSerializer<T>,
        respondMethod: suspend (args: List<*>) -> T
    ): ByteArray {
        val parsed = try {
            Rpc.fromByteArray(request, setup.format, argDeserializers)
        } catch (e: SerializationException) {
            throw InvalidRpcRequestException("Malformed request arguments: ${e.message}", e)
        }

        println("Running ${parsed.method}()")

        return setup.format.encode(resultSerializer, respondMethod(parsed.arguments))
    }

    public suspend fun <T> transformEvent(
        setup: AnyRpcServerSetup,
        subscriptionData: ByteArray,
        argDeserializers: List<KSerializer<*>>,
        resultSerializer: KSerializer<T>,
        transform: suspend (args: List<*>) -> T
    ): ByteArray {
        val parsed = setup.format.decode(TupleSerializer(argDeserializers), subscriptionData)
        return setup.format.encode(resultSerializer, transform(parsed))
    }

    public suspend fun invokeTargetedEvent(event: String, dispatcherData: List<*>, setup: AnyRpcServerSetup, target: Any?) {
        // Note that null -> "null" as the target
        invokeEventImpl(event, dispatcherData, setup, target.toString())
    }

    public suspend fun invokeEvent(event: String, dispatcherData: List<*>, setup: AnyRpcServerSetup) {
        invokeEventImpl(event, dispatcherData, setup, target = null)
    }

    /**
     * It's important to differentiate between there being no target (target = null) and the developer passing null as the target
     * (target = "null"). Note that will still interpret passing null and "null" as the developer as the same thing.
     */
    private suspend fun invokeEventImpl(event: String, dispatcherData: List<*>, setup: AnyRpcServerSetup, target: String?) {
        for (subscriber in setup.engine.eventManager.match(event, target)) {
            val transformed = setup.transformEvent(dispatcherData, subscriber.info) ?: error("RPC4k Error: could not find invoked event '${event}'")
            subscriber.connection.send(transformed)
        }
    }
}


