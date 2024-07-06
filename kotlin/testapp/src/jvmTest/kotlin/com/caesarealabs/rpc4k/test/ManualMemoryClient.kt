package com.caesarealabs.rpc4k.test

import com.caesarealabs.rpc4k.generated.BasicApiClient
import com.caesarealabs.rpc4k.runtime.api.GeneratedClientImplFactory
import com.caesarealabs.rpc4k.runtime.api.HandlerConfig
import com.caesarealabs.rpc4k.runtime.api.RpcClient
import com.caesarealabs.rpc4k.runtime.api.SerializationFormat
import com.caesarealabs.rpc4k.runtime.user.EventSubscription
import com.caesarealabs.rpc4k.testapp.BasicApi
import com.caesarealabs.rpc4k.testapp.Dog
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow

//LOWPRIO: Improve server testing with "in-memory-server" client generation
// below is a proof of concept of a direct interface with the service via memory that doesn't do any serialization.
// It is quite complicated as it involves having a new interface for both the client and invoker, and a new implementation for them.
// Additionally, the server must expose its `Invoker` via some interface for this to work.
// This approach is more efficient but it tests less things, so I'm keeping it here for when I need it but for now I'll use
// a memory implementation of a RpcClient / RpcServer that still does serialization.

class BasicApiMemoryClient(private val server: BasicApi) : BasicApiClient {
    override suspend fun getDogs(num: Int, type: String): List<Dog> = server.getDogs(num, type)

    override suspend fun putDog(dog: Dog): Unit = server.putDog(dog)

    override fun dogEvent(target: String, clientParam: Boolean): EventSubscription<Int> {
        val flow = callbackFlow {
            val invoker = (server.invoker as BasicApiMemoryEventInvoker)
            val subscription = MemoryEventSubscription(listOf(clientParam)) {
                send(it as Int)
            }
            invoker.dogSubscriptions.add(subscription)
            awaitClose { invoker.dogSubscriptions.remove(subscription) }
        }
        // We don't care about listenerIds in a memory client
        return EventSubscription(listenerId = "<Memory>", flow)
    }
}

class MemoryEventSubscription(val data: List<Any?>, val callback: suspend (Any?) -> Unit)
public class BasicApiMemoryEventInvoker(
    private val config: HandlerConfig<BasicApi>,
) {
    val dogSubscriptions: MutableList<MemoryEventSubscription> = mutableListOf()

    /**
     * @param participants Listeners that will not be invoked as they have caused the event.
     */
    public suspend fun invokeDogEvent(
        dispatchParam: Int,
        target: String,
        participants: Set<String> = setOf(),
    ) {
        for (subscription in dogSubscriptions) {
            val emitted = config.handler.dogEvent(dispatchParam, target, subscription.data[0] as Boolean)
            subscription.callback(emitted)
        }
    }
}
