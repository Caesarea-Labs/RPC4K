//package io.github.natanfudge.rpc4k.runtime.api
//
///**
// * Accepted by generated API code to configure the [transmitter] and [format] that the API uses.
// * When this class is accepted by the generated API code, it means it does not require return values from RPCs, which means you can specify [RpcClient]s
// * that don't return values like WebSocket.
// */
//open class RpcConfiguration(open val transmitter: RpcClient, val format: SerializationFormat)
///**
// * Accepted by generated API code to configure the [transmitter] and [format] that the API uses.
// * When this class is accepted by the generated API code, it means it requires return values from RPCs, which means you must specify [RpcClient]s
// * that return values like Http clients.
// */
//class RespondingRpcConfiguration(override val transmitter: RespondingRpcClient, format: SerializationFormat) : RpcConfiguration(transmitter, format)