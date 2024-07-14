package com.caesarealabs.rpc4k.runtime.api.components

import com.benasher44.uuid.uuid4
import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.implementation.RpcResult
import kotlinx.serialization.KSerializer

// A global map used to store active MemoryMulticallServer by their 'port'
private val memoryServerRegistry = mutableMapOf<Int, MemoryMulticallServer>()


/**
 * Simple implementation of a [DedicatedServer] that handles everything in-memory without any need for HTTP and such
 * @param port While this doesn't actually open a port on the network, this number is used as an identifier for the server,
 * so that clients may connect to this server specifically and not other servers running in the same process.
 */
public class MemoryMulticallServer(private val port: Int) : DedicatedServer {
    private val connections = mutableMapOf<EventConnection, MemoryEventClient>()
    private var config: ServerConfig? = null

    private fun getConfig() = config ?: error("Attempt to respond with server that has not been started with start()")

    /**
     * Emulates the handling of a request by a server
     */
    internal suspend fun respond(rpcRequest: ByteArray): RpcResult {
        return RpcServerUtils.routeCall(rpcRequest, getConfig())
    }

    /**
     * Emulates the handling of a subscription / unsubscription by a server
     */
    internal suspend fun acceptEventMessage(message: ByteArray, session: MemoryEventClient) {
        getConfig().acceptEventSubscription(message, session.connection)
    }

    internal fun connect(client: MemoryEventClient) {
        connections[client.connection] = client
    }

    /**
     * Currently unused
     */
    internal fun disconnect(client: MemoryEventClient) {
        connections.remove(client.connection)
    }

    override fun start(config: ServerConfig, wait: Boolean) {
        this.config = config
        memoryServerRegistry[port] = this
    }

    override fun stop() {
        memoryServerRegistry.remove(port)
    }

    override suspend fun send(connection: EventConnection, bytes: ByteArray): Boolean {
        val session = connections[connection] ?: return false
        session.handleMessage(S2CEventMessage.fromByteArray(bytes))
        return true
    }
}

/**
 * Simple implementation of a [RpcClient] that handles everything in-memory without any need for HTTP and such
 * @param port While this doesn't actually open a port on the network, this number is used as an identifier for the server,
 * so that clients may connect to this server specifically and not other servers running in the same process.
 */
public class MemoryRpcClient(private val port: Int) : RpcClient {
    override suspend fun send(rpc: Rpc, format: SerializationFormat, serializers: List<KSerializer<*>>): ByteArray {
        val data = rpc.toByteArray(format, serializers)
        val server = memoryServerRegistry[port] ?: error("Cannot find started memory server at port $port")
        when (val response = server.respond(data)) {
            // The last 2 parameters don't mean much here
            is RpcResult.Error -> throw RpcResponseException(response.message, rpc, format, this, response.message, 0)
            is RpcResult.Success -> return response.bytes
        }
    }

    override val events: EventClient = MemoryEventClient(port)

}

internal class MemoryEventClient(private val port: Int) : AbstractEventClient() {
    // Used as an identifier for the connection to the server
    internal val connection = EventConnection(uuid4().toString())
    private var connected = false
    override suspend fun send(message: ByteArray) {
        val server = memoryServerRegistry[port] ?: error("Cannot find started memory server at port $port")
        if (!connected) server.connect(this)
        server.acceptEventMessage(message, this)
    }
}
