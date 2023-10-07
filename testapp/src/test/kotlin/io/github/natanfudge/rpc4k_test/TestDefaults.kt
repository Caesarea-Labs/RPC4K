package io.github.natanfudge.rpc4k_test

import io.github.natanfudge.rpc4k.runtime.api.old.Logger
import io.github.natanfudge.rpc4k.runtime.api.old.client.RpcHttpClient
import io.github.natanfudge.rpc4k.runtime.api.old.server.FlowRequestHandler
import io.github.natanfudge.rpc4k.runtime.api.old.server.RequestHandler
import io.github.natanfudge.rpc4k.runtime.api.old.server.RpcHttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

object InMemoryHttpServer : RpcHttpServer {
    private var handler: RequestHandler? = null
    private var flowHandler: FlowRequestHandler? = null
    override fun start(requestHandler: RequestHandler, flowRequestHandler: FlowRequestHandler) {
        handler = requestHandler
        flowHandler = flowRequestHandler
    }

    override fun stop() {
    }

    internal fun testClientHookForRequest(route: String, body: ByteArray): ByteArray {
        val call = handler ?: throw IllegalStateException("Server not started")
        return runBlocking { call(route, body) }
    }

    internal fun testClientHookForFlowRequest(route: String, body: ByteArray): Flow<ByteArray> {
        val call = flowHandler ?: throw IllegalStateException("Server not started")
        return runBlocking { call(route, body) }
    }
}

object InMemoryHttpClient : RpcHttpClient {
    override suspend fun request(route: String, body: ByteArray): ByteArray {
        return InMemoryHttpServer.testClientHookForRequest(route, body)
    }

    override suspend fun flowRequest(route: String, body: ByteArray): Flow<ByteArray> {
        return InMemoryHttpServer.testClientHookForFlowRequest(route, body)
    }
}


object TestClientLogger : Logger {
    private fun cprint(prefix: String, msg: () -> String) = println("C-$prefix: ${msg()}")
    override fun debug(msg: () -> String) {
        cprint("debug", msg)
    }

    override fun info(msg: () -> String) {
        cprint("info", msg)
    }

    override fun warn(msg: () -> String) {
        cprint("warn", msg)
    }

    override fun error(msg: () -> String) {
        cprint("error", msg)
    }
}

object TestServerLogger : Logger {
    private fun sprint(prefix: String, msg: () -> String) = println("S-$prefix: ${msg()}")
    override fun debug(msg: () -> String) {
        sprint("debug", msg)
    }

    override fun info(msg: () -> String) {
        sprint("info", msg)
    }

    override fun warn(msg: () -> String) {
        sprint("warn", msg)
    }

    override fun error(msg: () -> String) {
        sprint("error", msg)
    }
}

fun serverTest(test: suspend CoroutineScope.() -> Unit) {
    runBlocking {
        withTimeout(20_000) {
            test()
        }
    }
}