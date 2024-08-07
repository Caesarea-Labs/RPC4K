@file:Suppress("TestFunctionName")

package com.caesarealabs.rpc4k.testserver

import com.caesarealabs.rpc4k.generated.rpc4k
import com.caesarealabs.rpc4k.runtime.jvm.user.components.startKtor
import com.caesarealabs.rpc4k.testapp.AllEncompassingService
import com.caesarealabs.rpc4k.testapp.BasicApi
import kotlin.test.Test


class TestServers {
    @Test
    fun myApi() {
        BasicApi.rpc4k.startKtor { BasicApi(it) }
    }

    @Test
    fun allEncompassingService() {
       AllEncompassingService.rpc4k.startKtor { AllEncompassingService(it) }
    }
}