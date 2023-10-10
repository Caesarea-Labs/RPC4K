package io.github.natanfudge.rpc4k.runtime.api.old.client

sealed class RpcException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Exception) : super(message, cause)
}

class InternalServerException : RpcException("Internal Server Error")
class ExpectationFailedException : RpcException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Exception) : super(message, cause)
}

class OtherStatusCodeException(code: Int) : RpcException("Unexpected status code: $code")
class UnauthorizedException(message: String) : RpcException(message)
