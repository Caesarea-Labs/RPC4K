package com.caesarealabs.rpc4k.runtime.implementation

import com.caesarealabs.logging.LoggingFactory
import com.caesarealabs.logging.PrintLoggingFactory
import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.user.Rpc4kIndex
import com.caesarealabs.rpc4k.runtime.user.components.JsonFormat

public sealed interface RpcResult {
    public class Success(public val bytes: ByteArray) : RpcResult
    public class Error(public val message: String,public val errorType: RpcError) : RpcResult
}


public fun <S, I> Rpc4kIndex<S, *, I>.createHandlerConfig(
    eventManager: EventManager,
    engine: RpcMessageLauncher,
    logging: LoggingFactory = PrintLoggingFactory,
    format: SerializationFormat = JsonFormat(),
    service: (I) -> S
): TypedHandlerConfig<S, I> {
    return TypedHandlerConfig({ service(it) }, { createInvoker(it) }, format, eventManager, engine, logging)
}


