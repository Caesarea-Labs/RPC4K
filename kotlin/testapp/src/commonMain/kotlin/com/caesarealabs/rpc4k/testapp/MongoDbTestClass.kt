package com.caesarealabs.rpc4k.testapp

import com.caesarealabs.rpc4k.runtime.user.Api
import com.caesarealabs.rpc4k.runtime.user.EventTarget
import com.caesarealabs.rpc4k.runtime.user.RpcEvent

//TODO: Move to be a part of rpc4k itself
@Api
class MongoDbTestClass {
    companion object;
    @RpcEvent
    fun targetTest(@EventTarget x: Int): Int {
        return x
    }
    @RpcEvent
    fun noTargetTest(x: Int): Int {
        return x
    }
}