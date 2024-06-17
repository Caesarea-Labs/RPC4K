package com.caesarealabs.rpc4k.runtime.api

//TODO: this can probably be simplified
/**
 * Something that creates a [RpcClient], generally there's one [RpcClientFactory] per [RpcClient]
 * This interface is useful because it's often easier to specify a [RpcClient] than an instance of a [ServerExtension] because [ServerExtension]
 * often have many parameters.
 */
public interface RpcClientFactory {
    public fun build(url: String, websocketUrl: String): RpcClient


}
