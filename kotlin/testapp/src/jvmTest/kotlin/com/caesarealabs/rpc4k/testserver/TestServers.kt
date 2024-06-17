@file:Suppress("TestFunctionName")

package com.caesarealabs.rpc4k.testserver

import com.caesarealabs.rpc4k.generated.rpc4k
import com.caesarealabs.rpc4k.runtime.jvm.api.KtorManagedRpcServer
import com.caesarealabs.rpc4k.runtime.user.startDedicatedServer
import com.caesarealabs.rpc4k.testapp.AllEncompassingService
import com.caesarealabs.rpc4k.testapp.BasicApi
import kotlin.test.Test

class TestServers {
    @Test
    fun myApi() {
        BasicApi.rpc4k.startDedicatedServer(engine = KtorManagedRpcServer()) { BasicApi() }
    }

    @Test
    fun allEncompassingService() {
        AllEncompassingService.rpc4k.startDedicatedServer(engine = KtorManagedRpcServer()) { AllEncompassingService(it) }
    }
}