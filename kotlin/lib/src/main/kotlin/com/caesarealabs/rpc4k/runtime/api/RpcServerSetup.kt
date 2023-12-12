package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.api.components.KtorManagedRpcServer
import com.caesarealabs.rpc4k.runtime.implementation.handleImpl

class RpcServerSetup<T, Engine : RpcServerEngine>(
    val handler: T,
    val generatedHelper: GeneratedServerHelper<T>,
    val engine: Engine,
    val format: SerializationFormat = JsonFormat()
) {
    companion object {
        // i want to get rid of this, not a very great api?
        fun <T> managedKtor(
            handler: T, generatedHelper: GeneratedServerHelper<T>, format: SerializationFormat = JsonFormat(),
            ktorServer: KtorManagedRpcServer = KtorManagedRpcServer()
        ): RpcServerSetup<T, KtorManagedRpcServer> {
            return RpcServerSetup(handler, generatedHelper, ktorServer, format)
        }
    }
}

fun <OldEngine : RpcServerEngine, NewEngine : RpcServerEngine, H> RpcServerSetup<H, OldEngine>.withEngine(engine: NewEngine) =
    RpcServerSetup(handler, generatedHelper, engine, format)


fun <Engine : RpcServerEngine.MultiCall> RpcServerSetup<*, Engine>.createServer(): RpcServerEngine.MultiCall.Instance = engine.create(this)
fun <Engine : RpcServerEngine.MultiCall> RpcServerSetup<*, Engine>.startServer(wait: Boolean)= engine.create(this)
    .start(wait)

suspend fun <RpcDef, I, O, Engine : RpcServerEngine.SingleCall.Writing<I, O>> RpcServerSetup<RpcDef, Engine>.handle(input: I, output: O) {
    handleImpl(input, output)
}

suspend fun <RpcDef, I, O, Engine : RpcServerEngine.SingleCall.Returning<I, O>> RpcServerSetup<RpcDef, Engine>.handle(input: I): O {
    return handleImpl(input, null)!!
}