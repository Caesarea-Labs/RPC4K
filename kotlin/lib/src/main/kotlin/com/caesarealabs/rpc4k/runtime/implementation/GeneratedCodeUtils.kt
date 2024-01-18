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
//        export function createObservable<T>(client: RpcClient, format: SerializationFormat, event: string, args: unknown[],
//                                        argSerializers: TsSerializer<unknown>[], eventSerializer: TsSerializer<T>,
//                                        target?: unknown): Observable<T> {
//        const listenerId = client.events.generateUuid()
//        const payload = format.encode(new TupleSerializer(argSerializers), args)
//        return client.events.createObservable(
//            //TODO: this probably breaks in binary formats
//            // This byte -> string conversion is prob inefficient
//            `sub:${event}:${listenerId}:${String(target) ?? ""}:${textDecoder.decode(payload)}`,
//            `unsub:${event}:${listenerId}`,
//            listenerId
//            //TODO: this string -> bytes conversion is also inefficient
//        ).map((value) => format.decode(eventSerializer, textEncoder.encode(value)))
//    }

    public suspend fun <T> createFlow(
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


