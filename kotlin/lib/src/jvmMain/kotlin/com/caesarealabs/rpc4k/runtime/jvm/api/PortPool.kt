package com.caesarealabs.rpc4k.runtime.jvm.api

public object PortPool {
    private var current = 8080

    @Synchronized
    public fun get(): Int {
        if (current == 8200) error("Too many ports are being requested")
        return current++
    }
}
