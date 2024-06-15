package com.caesarealabs.rpc4k.runtime.user

import com.caesarealabs.rpc4k.runtime.api.SerializationFormat
import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.user.testing.RpcClientFactory

public fun <S, C, I> Rpc4kIndex<S, C, I>.client(
    url: String,
    client: RpcClientFactory = RpcClientFactory.OkHttp(),
    format: SerializationFormat = JsonFormat(),
): C {
    val websocketUrl = "$url/events"
    val clientSetup = client.build(url, websocketUrl)
    return createNetworkClient(clientSetup, format)
}