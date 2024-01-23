//package com.caesarealabs.rpc4k.runtime.implementation
//
//import com.caesarealabs.rpc4k.runtime.api.RpcServerEngine
//import org.junit.jupiter.api.extension.AfterAllCallback
//import org.junit.jupiter.api.extension.BeforeAllCallback
//import org.junit.jupiter.api.extension.Extension
//import org.junit.jupiter.api.extension.ExtensionContext
//
//
//internal class MultiCallServerExtension<Api, I>(private val server: RpcServerEngine.MultiCall.Instance) : Extension, BeforeAllCallback, AfterAllCallback {
////    private val server = engine.createServer()
//    override fun beforeAll(context: ExtensionContext?) {
//        server.start(wait = false)
//    }
//
//    override fun afterAll(context: ExtensionContext?) {
//        server.stop()
//    }
//
//}
//
