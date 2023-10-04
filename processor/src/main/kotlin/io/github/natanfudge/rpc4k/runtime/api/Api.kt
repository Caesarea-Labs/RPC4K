package io.github.natanfudge.rpc4k.runtime.api
import io.github.natanfudge.rpc4k.runtime.api.client.ExpectationFailedException
import io.github.natanfudge.rpc4k.runtime.api.client.JvmProtocolFactory
import io.github.natanfudge.rpc4k.runtime.api.client.RpcException
import io.github.natanfudge.rpc4k.runtime.api.server.RpcServer


/**
 * Rpc4K provides an easy method of establishing communication between clients and a server.
 *
 * Step 1: Define an interface `MyApi` (or abstract/open class) that defines actions a client may perform with a server.
 * This interface is shared between the client and the server.
 *
 * Step 2: Annotate `MyApi` with @[Api]
 *
 * Step 3: Implement `MyApi` interface on the server and provide it to the generated `MyApiDecoder`,
 * and provide the decoder to a [RpcServer]. (i.e. `RpcServer(decoder = MyApiDecoder(protocol = <implementation>)`)
 * The implementation may also be present on `MyApi` itself, as long as all the functions are declared as `open`.
 *
 * Step 4: Instantiate the generated `MyApiClientImpl` on the client and use it.
 * Since generated classes are not currently resolved in the IDE, use [JvmProtocolFactory].
 *
 * - All api methods must be `suspend` because in practice we are making HTTP requests.
 *
 *
 * **Error handling:**
 *
 * *On the server*: Use the kotlin [require]* functions to validate client requests, or throw [IllegalArgumentException]s
 *
 * *On the client*: Handle server errors by catching [RpcException]s.
 * Failed requirements are thrown as [ExpectationFailedException]s.
 *
 * **Flows:**
 *
 * A method may return a **Flow** and it will work as expected. The flow is *cold*.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Api

