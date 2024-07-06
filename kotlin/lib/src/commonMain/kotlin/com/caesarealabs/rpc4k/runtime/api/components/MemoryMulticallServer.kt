//package com.caesarealabs.rpc4k.runtime.api.components
//
//import com.caesarealabs.rpc4k.runtime.api.EventConnection
//import com.caesarealabs.rpc4k.runtime.api.PortPool
//import com.caesarealabs.rpc4k.runtime.api.RpcServerEngine
//import com.caesarealabs.rpc4k.runtime.api.ServerConfig
//
//// A global map used to store active MemoryMulticallServer by their 'port'
//private val memoryServerRegistry = mutableMapOf<Int, MemoryMulticallServer.Instance>()
//
//internal  class MemorySession(val callback: (ByteArray) -> Unit) {
//
//}
//
///**
// * Simple implementation of a [RpcServerEngine.Dedicated] that handles everything in-memory without any need for HTTP and such
// * @param port While this doesn't actually open a port on the network, this number is used as an identifier for the server,
// * so that clients may connect to this server specifically and not other servers running in the same process.
// */
//public class MemoryMulticallServer(public val port: Int = PortPool.get()) : RpcServerEngine.Dedicated {
//    internal companion object {
//
//    }
//    internal inner class Instance : RpcServerEngine.Dedicated.Instance {
//        override fun start(wait: Boolean) {
//            memoryServerRegistry[port]  = this
//        }
//
//        override fun stop() {
//            TODO("Not yet implemented")
//        }
//
//    }
//
//    private val connections = mutableMapOf<EventConnection, DefaultWebSocketSession>()
//
//
//    override fun create(config: ServerConfig): RpcServerEngine.Dedicated.Instance = Instance()
//
//    override suspend fun sendMessage(connection: EventConnection, bytes: ByteArray): Boolean {
//        return connections[connection]?.send(Frame.Text(true, bytes)) != null
//    }
//}
