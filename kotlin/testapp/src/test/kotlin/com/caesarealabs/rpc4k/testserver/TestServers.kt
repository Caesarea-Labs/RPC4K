@file:Suppress("TestFunctionName")

package com.caesarealabs.rpc4k.testserver

import com.caesarealabs.rpc4k.testapp.AllEncompassingService
import com.caesarealabs.rpc4k.generated.server
import com.caesarealabs.rpc4k.runtime.api.RpcServerSetup
import com.caesarealabs.rpc4k.runtime.api.startServer
import com.caesarealabs.rpc4k.test.BasicApi
import kotlin.test.Test

class TestServers {
    @Test
    fun myApi() {
        RpcServerSetup.managedKtor({ BasicApi() }, BasicApi.server()).startServer(wait = true)
    }

    @Test
    fun allEncompassingService() {
        RpcServerSetup.managedKtor({ AllEncompassingService(invoker = it) }, AllEncompassingService.server()).startServer(wait = true)
    }
}