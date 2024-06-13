package com.caesarealabs.rpc4k.runtime.implementation

import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.implementation.serializers.TupleSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.*
import java.util.*

/**
 * These functions are used by generated code and code that interacts with them
 */
//TODO: consider making the C2S functions be in a instance method, this would allow storing the format and client and reduce codegen.
public object GeneratedCodeUtils {
    @PublishedApi
    internal const val FactoryName: String = "Factory"

    @PublishedApi
    internal const val ClientSuffix: String = "Client"

    @PublishedApi
    internal const val ServerSuffix: String = "Router"

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
     * @param target Note that here we don't have an issue with 'empty string' conflicting with 'no target' because
     * the server already defines when a target is necessary. When a target is needed, empty string is interpreted as empty string,
     * when a target is not needed, empty string, null, or anything else will be treated as 'no target'.
     */
    public fun <T> coldEventFlow(
        client: RpcClient,
        format: SerializationFormat,
        event: String,
        args: List<*>,
        argSerializers: List<KSerializer<*>>,
        eventSerializer: KSerializer<T>,
        target: Any? = null
    ): Flow<T> {
        val listenerId = UUID.randomUUID().toString()
        val payload = format.encode(TupleSerializer(argSerializers), args)
        val subscriptionMessage = C2SEventMessage.Subscribe(event = event, listenerId = listenerId, payload, target.toString())
        val unsubMessage = C2SEventMessage.Unsubscribe(event = event, listenerId = listenerId)
        return client.events.createFlow(subscriptionMessage.toByteArray(), unsubMessage.toByteArray(), listenerId)
            .map { format.decode(eventSerializer, it) }
    }

    /**
     * Uses the [server] to respond with the specified data
     */
    public suspend fun <T> respond(
        config: HandlerConfig<*>,
        request: ByteArray,
        argDeserializers: List<KSerializer<*>>,
        resultSerializer: KSerializer<T>,
        respondMethod: suspend (args: List<*>) -> T
    ): ByteArray {
        val parsed = try {
            Rpc.fromByteArray(request, config.format, argDeserializers)
        } catch (e: SerializationException) {
            throw InvalidRpcRequestException("Malformed request arguments: ${e.message}", e)
        }

        println("Running ${parsed.method}()")

        return config.format.encode(resultSerializer, respondMethod(parsed.arguments))
    }

    public suspend fun <Server, R> invokeEvent(
        config: HandlerConfig<Server>,
        eventName: String,
        subArgDeserializers: List<KSerializer<*>>,
        resultSerializer: KSerializer<R>,
        /**
         * Important - pass null when targets are not used in the event,
         * pass .toString() when targets are used in the event. The null value should be equivalent to the "null" value, when targets are relevant.
         */
        target: String? = null,
        handle: suspend (subArgs: List<*>) -> R
    ) {
        println("Invoking event $eventName")
        for (subscriber in config.eventManager.match(eventName, target)) {
            val parsed = config.format.decode(TupleSerializer(subArgDeserializers), subscriber.info.data)
            val handled = handle(parsed)
            val bytes = config.format.encode(resultSerializer, handled)
            val fullMessage = S2CEventMessage.Emitted(subscriber.info.listenerId, bytes).toByteArray()
            config.engine.sendMessage(subscriber.connection,fullMessage)
//             subscriber.connection.send(fullMessage)
        }
    }
}
