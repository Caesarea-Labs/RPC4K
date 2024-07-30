package com.caesarealabs.rpc4k.runtime.implementation

import com.caesarealabs.logging.LoggingFactory
import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.user.Rpc4kIndex

public sealed interface RpcResult {
    public class Success(public val bytes: ByteArray) : RpcResult
    public class Error(public val message: String,public val errorType: RpcError) : RpcResult
}


internal fun <S, I> Rpc4kIndex<S, *, I>.createHandlerConfig(
    format: SerializationFormat,
    eventManager: EventManager,
    engine: RpcMessageLauncher,
    logging: LoggingFactory,
    service: (I) -> S
): HandlerConfigImpl<S, I> {
    @Suppress("RemoveExplicitTypeArguments")
    // The type args are necessary.
    return HandlerConfigImpl<S, I>({ service(it) }, { createInvoker(it) }, format, eventManager, engine, logging)
}


