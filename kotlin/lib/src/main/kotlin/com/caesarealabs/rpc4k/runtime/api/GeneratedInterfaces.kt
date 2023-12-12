package com.caesarealabs.rpc4k.runtime.api

/**
 * All generated server classes implement this interface, to make usage easier.
 * Some api methods use this interface, but you can also use it yourself
 */
interface GeneratedServerHelper<T> {
    suspend fun handle(request: ByteArray, method: String, setup: RpcServerSetup<T, *>): ByteArray?
}


/**
 * All generated client classes have a Factory that implement this interface, and that factory of course creates the [Api]
 * with the given format, and RpcClient.
 * Some api methods accept this factory since it's much easier to just pass a factory rather than the [Api] instance itself
 * since actually creating a [Api] can be hard.
 */
interface GeneratedClientImplFactory<Api> {
    fun build(client: RpcClient, format: SerializationFormat): Api
}