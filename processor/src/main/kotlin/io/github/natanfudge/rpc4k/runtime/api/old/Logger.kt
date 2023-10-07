package io.github.natanfudge.rpc4k.runtime.api.old

interface Logger {
    fun debug(msg: () -> String)
    fun info(msg: () -> String)
    fun warn(msg: () -> String)
    fun error(msg: () -> String)
}