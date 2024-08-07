package com.caesarealabs.rpc4k.runtime.implementation

import com.benasher44.uuid.uuid4
import com.caesarealabs.logging.Logging
import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.implementation.serializers.TupleSerializer
import com.caesarealabs.rpc4k.runtime.user.EventSubscription
import com.caesarealabs.rpc4k.runtime.user.RPCContext
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException

/**
 * These functions are used by generated code and code that interacts with them
 */
//TO DO: consider making the C2S functions be in a instance method, this would allow storing the format and client and reduce codegen.
public object GeneratedCodeUtils {


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
     * Creates a new [EventSubscription] that is a _cold_ flow that allows listening to an event.
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
    ): EventSubscription<T> {
        val listenerId = uuid4().toString()
        val payload = format.encode(TupleSerializer(argSerializers), args)
        val subscriptionMessage = C2SEventMessage.Subscribe(event = event, listenerId = listenerId, payload, target?.toString())
        val unsubMessage = C2SEventMessage.Unsubscribe(event = event, listenerId = listenerId)
        val flow = client.events.createFlow(subscriptionMessage.toByteArray(), unsubMessage.toByteArray(), listenerId)
            .map { format.decode(eventSerializer, it) }
        return EventSubscription(listenerId, flow)
    }

    /**
     * Called by the generated Router, to respond to the client after receiving a request.
     * [argDeserializers], [resultSerializer], and [respondMethod] are unique for each procedure
     * and so need special codegen to generate them.
     */
    public suspend fun <T> respond(
        config: HandlerConfig<*>,
        request: ByteArray,
        argDeserializers: List<KSerializer<*>>,
        resultSerializer: KSerializer<T>,
        context: RPCContext,
        respondMethod: suspend RPCContext.(args: List<*>) -> T
    ): ByteArray {
        val parsed = try {
            Rpc.fromByteArray(request, config.format, argDeserializers)
        } catch (e: SerializationException) {
            throw InvalidRpcRequestException("Malformed request arguments: ${e.message}", e)
        }

        context.logData("Parameters") { parsed.arguments }
        val result = with(context) { respondMethod(parsed.arguments) }
        context.logData("Response") { result }
        val response = config.format.encode(resultSerializer, result)

        return response
    }

    public suspend fun <Server, R> invokeEvent(
        config: HandlerConfig<Server>,
        eventName: String,
        subArgDeserializers: List<KSerializer<*>>,
        resultSerializer: KSerializer<R>,
        /**
         * The actors that actually produced this event, and will not want to get updated that this event occurred, because they
         * already updated the outcome of said event in memory.
         */
        participants: Set<String>,
        /**
         * Important - pass null when targets are not used in the event,
         * pass .toString() when targets are used in the event. The null value should be equivalent to the "null" value, when targets are relevant.
         */
        target: String? = null,
        handle: suspend (subArgs: List<*>) -> R
    ) {
        val match = config.eventManager.match(eventName, target)
        config.logging.wrapCall(eventName) {
            for (subscriber in match) {
                // Don't send events to participants
                if (subscriber.info.listenerId in participants) continue

                val parsed = config.format.decode(TupleSerializer(subArgDeserializers), subscriber.info.data)
                logData("Listener ID") { subscriber.info.listenerId }
                logData("Subscription Data") { subscriber.info.data }

                logInfo { "Processing subscription ${subscriber.info.listenerId}" }
                val handled = handle(parsed)
                logData("Event") { handled }
                val bytes = config.format.encode(resultSerializer, handled)
                val fullMessage = S2CEventMessage.Emitted(subscriber.info.listenerId, bytes).toByteArray()
                config.sendOrDrop(subscriber.connection, fullMessage, this@wrapCall)
            }
        }

    }
}

/**
 * Will send the [bytes] to the [connection], dropping it if it cannot be reached
 */
internal suspend fun <T> HandlerConfig<T>.sendOrDrop(connection: EventConnection, bytes: ByteArray, logging: Logging) {
    val clientExists = messageLauncher.send(connection, bytes)
    if (!clientExists) {
        logging.logInfo { "Dropping connection ${connection.id} as it cannot be reached" }
        eventManager.dropClient(connection)
    }
}

