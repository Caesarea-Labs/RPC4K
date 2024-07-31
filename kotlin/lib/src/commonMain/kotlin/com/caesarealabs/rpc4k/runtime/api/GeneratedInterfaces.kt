package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.user.RPCContext

/**
 * Implemented by all generated Routers
 */
public interface RpcRouter<T> {
    public suspend fun routeRequest(request: ByteArray, method: String, config: HandlerConfig<T>, context: RPCContext): ByteArray?
//    public suspend fun handleEvent(dispatcherData: List<*>,
//                                   subscriptionData: ByteArray,
//                                   event: String,
//                                   setup: HandlerConfig<T>): ByteArray?
//    public fun createInvoker(setup: RpcSetupOf<out T>): Invoker
}



/**
 * All generated client classes have a Factory that implement this interface, and that factory of course creates the [Api]
 * with the given format, and RpcClient.
 * Some api methods accept this factory since it's much easier to just pass a factory rather than the [Api] instance itself
 * since actually creating a [Api] can be hard.
 */
public interface GeneratedClientImplFactory<Client> {
    public fun build(client: RpcClient, format: SerializationFormat): Client
}