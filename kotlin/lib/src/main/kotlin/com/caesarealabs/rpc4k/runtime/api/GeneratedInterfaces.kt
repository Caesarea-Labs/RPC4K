package com.caesarealabs.rpc4k.runtime.api

/**
 * All generated server classes implement this interface, to make usage easier.
 * Some api methods use this interface, but you can also use it yourself
 */
public interface GeneratedServerHelper<T, Invoker> {
    public suspend fun handleRequest(request: ByteArray, method: String, setup: RpcSetupOf<out T>): ByteArray?
    public suspend fun handleEvent(dispatcherData: List<*>,
                                   subscriptionData: ByteArray,
                                   event: String,
                                   setup: RpcSetupOf<out T>): ByteArray?
    public fun createInvoker(setup: RpcSetupOf<out T>): Invoker
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