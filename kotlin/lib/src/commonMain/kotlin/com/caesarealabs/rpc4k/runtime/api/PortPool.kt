package com.caesarealabs.rpc4k.runtime.api

import kotlinx.atomicfu.atomic

public object PortPool {
    private val current = atomic(8080)

    public fun get(): Int  = current.getAndIncrement()
}
