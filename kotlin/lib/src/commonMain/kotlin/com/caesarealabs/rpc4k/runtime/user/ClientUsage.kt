package com.caesarealabs.rpc4k.runtime.user

import com.caesarealabs.rpc4k.runtime.api.RpcClientFactory
import com.caesarealabs.rpc4k.runtime.api.SerializationFormat
import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat

public fun <S, C, I> Rpc4kIndex<S, C, I>.client(
    url: String,
//     TODO: MPP default
    client: RpcClientFactory /*= RpcClientFactory.OkHttp()*/,
    format: SerializationFormat = JsonFormat(),
    x: Int = 2
): C {
    val websocketUrl = "$url/events"
    val clientSetup = client.build(url, websocketUrl)
    return createNetworkClient(clientSetup, format)
}


