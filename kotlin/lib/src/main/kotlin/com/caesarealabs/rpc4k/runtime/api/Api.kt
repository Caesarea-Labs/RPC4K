package com.caesarealabs.rpc4k.runtime.api


/**
 * Generates a class that may be used to access an RPC server.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class Api(val generateClient: Boolean = false)

//TODO: document
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
public annotation class RpcEvent

//TODO: document
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
public annotation class WatchedValue

//TODO: document
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
public annotation class Dispatch
