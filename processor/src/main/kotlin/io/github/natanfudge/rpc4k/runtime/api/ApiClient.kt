package io.github.natanfudge.rpc4k.runtime.api

import io.github.natanfudge.rpc4k.runtime.api.old.client.ExpectationFailedException
import io.github.natanfudge.rpc4k.runtime.api.old.client.JvmProtocolFactory
import io.github.natanfudge.rpc4k.runtime.api.old.client.RpcException
import io.github.natanfudge.rpc4k.runtime.api.old.server.RpcServer




/***
 * Generates a class that may be used to access an RPC server.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ApiClient

/***
 * Generates a class that may act as an RPC server.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ApiServer

