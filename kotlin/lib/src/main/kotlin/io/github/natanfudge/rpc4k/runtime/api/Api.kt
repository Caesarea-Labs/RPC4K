package io.github.natanfudge.rpc4k.runtime.api


/**
 * Generates a class that may be used to access an RPC server.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Api(@Suppress("unused") val generateClient: Boolean = false)

