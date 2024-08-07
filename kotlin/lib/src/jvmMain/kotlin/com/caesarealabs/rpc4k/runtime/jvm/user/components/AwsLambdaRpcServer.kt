package com.caesarealabs.rpc4k.runtime.jvm.user.components

import aws.sdk.kotlin.services.apigatewaymanagementapi.ApiGatewayManagementClient
import aws.sdk.kotlin.services.apigatewaymanagementapi.model.GoneException
import aws.sdk.kotlin.services.apigatewaymanagementapi.postToConnection
import aws.smithy.kotlin.runtime.net.url.Url
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse
import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.implementation.RpcResult

public object Rpc4kAwsLambda {
    public suspend fun routeCalls(call: APIGatewayV2HTTPEvent, config: ServerConfig): APIGatewayV2HTTPResponse {
        if (call.headers == null) return invalidRequest("Missing API Gateway Headers")
        if (call.body == null) return invalidRequest("Missing API Gateway body")
        // Lambda behaves unexpectedly when content-type is not specified (as json when usning json, etc)
        if (!call.headers.mapKeys { (k, _) -> k.lowercase() }.containsKey("content-type")) {
            return invalidRequest(
                "No Content-Type header was specified, so the request can't be interpreted properly. Existing headers:" +
                        " ${call.headers}"
            )
        }

        val bytes = toByteArray(call)
        return when (val result = RpcServerUtils.routeCall(bytes, config)) {
            is RpcResult.Error -> createErrorResponse(result.message, result.errorType)
            is RpcResult.Success -> createResponse(result.bytes)
        }
    }

    private fun invalidRequest(message: String) = APIGatewayV2HTTPResponse.builder()
        .withBody(message)
        .withStatusCode(400)
        .build()

    /**
     * Entrypoint for exposing an RPC server's events using AWS websockets.
     * If called, will redirect the websocket call to the RPC mechanism and call the appropriate function, subscribing to the event.
     * The caller will receive events matching his request ID specified in the body of the subscription request.
     * The server, usually called with AWS Lambda, will then use the AWS API to send that specific listener the data, when an event occurs.
     */
    public suspend fun acceptWebsocketSubscription(event: APIGatewayV2WebSocketEvent, config: ServerConfig): APIGatewayV2WebSocketResponse {
        // We use APIGatewayV2WebSocketEvent.requestContext.connectionId as the unique identifier for the connection itself
        val connection = EventConnection(event.requestContext.connectionId)
        when (event.requestContext.routeKey) {
            "\$connect" -> {
                // Don't do anything special on connect
            }

            "\$disconnect" -> {
                // Clean up the client subscriptions in case it hasn't unsubscribed itself
                config.config.eventManager.dropClient(connection)
            }

            else -> {
                config.acceptEventSubscription(event.body.toByteArray(), connection)
            }
        }

        return APIGatewayV2WebSocketResponse().apply {
            statusCode = 200
        }
    }

    private fun toByteArray(call: APIGatewayV2HTTPEvent): ByteArray {

        //TO DO: what happens in binary formats?
        return call.body.toByteArray()
    }

    /**
     * Convert a [ByteArray] result into an [APIGatewayV2HTTPResponse]
     */
    private fun createResponse(body: ByteArray): APIGatewayV2HTTPResponse {
        return APIGatewayV2HTTPResponse.builder()
            //TO DO: what happens in binary formats?
            .withBody(body.decodeToString())
            .withStatusCode(200)
            .build()
    }

    /**
     * Convert an [RpcError] + String into a [APIGatewayV2HTTPResponse]
     */
    private fun createErrorResponse(message: String, errorType: RpcError): APIGatewayV2HTTPResponse {
        val code = when (errorType) {
            RpcError.InvalidRequest -> 400
            RpcError.InternalError -> 500
        }
        return APIGatewayV2HTTPResponse.builder()
            .withBody(message)
            .withStatusCode(code)
            .build()
    }
}

public class ApiGatewayMessageLauncher(region: String, websocketConnectionUrl: String) : RpcMessageLauncher {
    /**
     * AWS SDK instance that allows POSTing to an existing websocket connection, required for events.
     */
    private val apiGatewayClient = ApiGatewayManagementClient {
        this.region = region
        this.endpointUrl = Url.parse(websocketConnectionUrl)
    }

    /**
     * In order to emit events, POSTs to an existing websocket connection
     */
    override suspend fun send(connection: EventConnection, bytes: ByteArray): Boolean {
        try {
            apiGatewayClient.postToConnection {
                connectionId = connection.id
                data = bytes
            }
            return true
        } catch (e: GoneException) {
            return false
        }
    }

}


