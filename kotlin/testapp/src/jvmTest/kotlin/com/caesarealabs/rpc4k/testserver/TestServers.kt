@file:Suppress("TestFunctionName")

package com.caesarealabs.rpc4k.testserver

import com.caesarealabs.rpc4k.generated.rpc4k
import com.caesarealabs.rpc4k.runtime.jvm.api.KtorManagedRpcServer
import com.caesarealabs.rpc4k.runtime.user.startRpc
import com.caesarealabs.rpc4k.testapp.AllEncompassingService
import com.caesarealabs.rpc4k.testapp.BasicApi
import kotlin.test.Test

//TODO: refine these APIs


class TestServers {
    @Test
    fun myApi() {
        KtorManagedRpcServer().startRpc(BasicApi.rpc4k) { BasicApi(it) }
    }

    @Test
    fun allEncompassingService() {
        KtorManagedRpcServer().startRpc(AllEncompassingService.rpc4k) { AllEncompassingService(it) }
    }
}