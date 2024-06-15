package com.caesarealabs.rpc4k.runtime.api

import kotlinx.coroutines.flow.Flow

public interface EventClient {
    public suspend fun send(message: ByteArray)

    /**
     * Creates a _cold_ `Flow` that listens to new events
     */
    public  fun createFlow(subscribeMessage: ByteArray, unsubscribeMessage: ByteArray, listenerId: String): Flow<ByteArray>
}

//    events: EventClient
//}
//
//export interface EventClient {
//    send(message: string): Promise<void>
//    createObservable(subscribeMessage: string, unsubscribeMessage: string, listenerId: string): Observable<string>
//
//    /**
//     * Since UUID sources are different in browser and in node we need to generate a uuid differently per client
//     */
//    generateUuid(): string
//}

//
//val webSocket = OkHttpClient().newWebSocket(Request("http://localhost:8080/events".toHttpUrl()), object: WebSocketListener() {
//    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
//        println("Got message: ${bytes.string(Charset.defaultCharset())}")
//    }
//
//    override fun onMessage(webSocket: WebSocket, text: String) {
//        actualMessage = text
//    }
//})
//
//
//webSocket.send("sub:eventTest:121b9a71-20f6-4d6c-91a2-4f0f1550d9ac::[\"Test string\"]")