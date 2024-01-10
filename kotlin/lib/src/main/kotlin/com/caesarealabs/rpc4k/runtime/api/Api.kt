package com.caesarealabs.rpc4k.runtime.api


/**
 * Generates a class that may be used to access an RPC server.
 *
 * Commonly, your @[Api] class will want to accept the generated Invoker as a parameter.
 * A good way to do it is like so:
 * ```kotlin
 * @Api
 * open class MyService(val invoker: MyServiceEventInvoker = MyServiceEventInvoker(null)) {
 *  // ...
 * }
 * ```
 * This way, when the service is created as a client a stub invoker will exist but won't be used.
 * When the service is created as a server explicitly by you,
 * you should pass the Invoker provided by the lambda in the RpcServerSetup instead,
 * as that invoker can actually invoke events, and can be used in your service implementation:
 * ```kotlin
 * val setup = RpcServerSetup({ MyService(it) }, ...)
 *```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class Api(val generateClient: Boolean = false)

/**
 * Marks the @[Api] method as an event.
 *
 * Events are subscribed to once on the client, and then the server may invoke an event (using the generated Invoker class),
 * which will call the annotated method and send the result back to the client.
 * Usually, the client subscribes to the event once and the server invokes it many times after.
 *
 * Event method parameters may be annotated with [EventTarget] for increased efficiency,
 * [Dispatch] to pass new information on every invocation,
 * or left without annotations to be a normal parameter to the subscription to the event.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
public annotation class RpcEvent

/**
 * Marks a primitive value as the target of the event, meaning if the event is invoked for target X, only subscriptions that
 * have specified their target as X will be invoked.
 *
 * This allows increasing the performance of certain events significantly.
 * Consider Google had a Google Sheets event called 'sheet_changed'.
 * If for every change in any sheet, all sheets would need to be checked in the event transformer, that would be extremely slow.
 * However, if a singular sheet would receive a unique 'sheet-id' as the target, then whenever the sheet changes only subscriptions to the same
 * sheet would be considered and it would be much more efficient.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
public annotation class EventTarget

/**
 * Marks an @[RpcEvent] function parameter as information passed by the server on dispatch, instead of by the client on subscriptions.
 *
 * Event parameters are split into two types:
 * - _Subscription_ parameter (default)
 * - _Dispatch_ parameter (annotated with @[Dispatch])
 *
 * Both types of parameters may be used by and event handler method to decide which data to return to the client, however they
 * differ in the way they are passed to the method.
 * - _Subscription_ parameters are passed **once** and **by the client** to the server. They are then stored and reused whenever
 * the event is invoked.
 * - _Dispatch_ parameters have a **new value** every invocation that is provided **by the server** whenever it dispatches/invokes an event.
 *
 * Take for an example an every that listens to changes to a table, but only when the item matches a certain query.
 * In this case, the event method should be defined like this:
 * ```kotlin
 * @RpcEvent suspend fun onTableChanged(query: String, @Dispatch change: TableChange) {
 *   // ...
 * }
 * ```
 *
 * The `query` is passed **once** on subscription, but the `change` is **different** for every time the table changes.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
public annotation class Dispatch
