package io.github.natanfudge.rpc4k.runtime.api

/**
 * All generated server classes implement this interface, to make usage easier.
 * Some api methods use this interface, but you can also use it yourself
 */
interface GeneratedServerHandler {
    suspend fun handle(request: ByteArray, method: String)
}
/**
 * All generated server classes have a Factory that implement this interface, and that factory of course creates the [GeneratedServerHandler]
 * with the given api, format, and RpcServer.
 * Some api methods accept this factory since it's much easier to just pass a factory rather than the [GeneratedServerHandler] instance itself
 * since actually creating a [GeneratedServerHandler] can be hard.
 */
interface GeneratedServerHandlerFactory<Api> {
    fun build(api: Api, format:  SerializationFormat, server: RpcServer): GeneratedServerHandler
}

/**
 * All generated client classes have a Factory that implement this interface, and that factory of course creates the [Api]
 * with the given format, and RpcClient.
 * Some api methods accept this factory since it's much easier to just pass a factory rather than the [Api] instance itself
 * since actually creating a [Api] can be hard.
 */
interface GeneratedClientImplFactory<Api> {
    fun build(client: RpcClient, format:  SerializationFormat): Api
}