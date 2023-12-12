package com.caesarealabs.rpc4k.runtime.implementation

object PortPool {
    private var current = 8080

    @Synchronized
    fun get(): Int {
        if (current == 8200) error("Too many ports are being requested")
        return current++
    }
}
