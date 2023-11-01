package io.github.natanfudge.rpc4k.runtime.implementation

import io.github.natanfudge.rpc4k.runtime.api.RpcServerEngine
import io.github.natanfudge.rpc4k.runtime.api.RpcServerSetup
import io.github.natanfudge.rpc4k.runtime.api.createServer
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext


class MultiCallServerExtension(setup: RpcServerSetup<*, RpcServerEngine.MultiCall>) : Extension, BeforeAllCallback, AfterAllCallback {
    private val server = setup.createServer()
    override fun beforeAll(context: ExtensionContext?) {
        server.start(wait = false)
    }

    override fun afterAll(context: ExtensionContext?) {
        server.stop()
    }

}

