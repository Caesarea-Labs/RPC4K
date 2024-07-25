package com.caesarealabs.rpc4k.test.rpc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.nio.charset.Charset

object NetworkTests {
    fun testWebsockets(service: FullService) = runTest {
        withContext(Dispatchers.Default) {
            val api = service.server

            var actualMessage: String? = null


            val webSocket = OkHttpClient().newWebSocket(
                Request("http://localhost:${service.port}/events".toHttpUrl()),
                object : WebSocketListener() {
                    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                        println("Got message: ${bytes.string(Charset.defaultCharset())}")
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        actualMessage = text
                    }
                })


            webSocket.send("sub:eventTest:121b9a71-20f6-4d6c-91a2-4f0f1550d9ac::[\"Test string\"]")
            delay(1000)

            api.tinkerWithEvents()

            delay(1000)

            expectThat(actualMessage).isEqualTo("event:121b9a71-20f6-4d6c-91a2-4f0f1550d9ac:\"Test string5\"")
        }
    }
}