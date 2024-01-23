package com.caesarealabs.rpc4k.runtime.api

public interface Rpc4kIndex<Server, Client, Invoker> {
    public val createNetworkClient: (rpcClient: RpcClient, format: SerializationFormat) -> Client
    //TODO:
//    public val createMemoryClient: (server: Server) -> Client
    public val createInvoker: (HandlerConfig<Server>) -> Invoker
    public val router: RpcRouter<Server>
}

private interface Context {
    val y: Server
}


private class Invoker(private val provider: Context) {
    fun bar() {
        provider.y.baz()
    }

}

private class Server(private val invoker: Invoker) {
    fun foo() {
        invoker.bar()
    }
    fun baz(){

    }
}


private fun create() {
    val p = object :Context {
        override val y: Server = Server(Invoker(this))
    }
}