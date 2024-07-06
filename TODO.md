

### Handle erroneous event subscriptions
1. If invoking an event fails, remove it from the subscription list and send an error message to the client signaling an error occurred. Make sure to pass a listener id. 
- If it's a format error with the subscription data, send a format error detailing what went wrong. If it's an internal error, simply say that there is an internal error. 
2. Handle format/internal error events in the client - print an error and remove from the listener lists. 
### Handle flushing out old event subscriptions
Maybe handling erronous events is good enough?

### Resolve the issue of invoker in direct server testing
When we have a service like this
```kotlin
class Foo(val invoker: FooInvoker) {
    fun bar() {
        
    }
}
```
We often want to just construct Foo and test it. For this reason we need to have some good instance of FooInvoker that will do what we expect. 
One option is to provide a HandlerConfig that uses some simple MemoryEventManager and json to send information back to the server,
Another option is to provide some totally in-memory implementation of the invoker to simplify debugging. We probably still need to use a MemoryEventManager.
The relation between this feature and the in-memory client is TBD. 



### Allow passing a list of participants on event invocation

Take the following scenario: 

- App has an editable table that the user can view
- Table is filled with data from the server, updated whenever someone edits the table

When a user edits the table, the app will update the table in memory for performance reasons. It will then send that update to the server, to update other users. 
The problem is - the server won't differentiate between who _participated_ in that editing event and therefore already knows it occurred, and between those that didn't participate and therefore need to be updated. The server will then send the edit update back to the editing client, which is redundant. 

**Solution**

1. Update the server's invoke method to this, updating the body to pass `participants` to `invokeEvent`:

```kotlin
suspend fun invokeSomeEvent(param1: Int, param2: String, participants: List<String> = listOf()) 
```

2. Do not send events to participants in the body of `invokeEvent`

3. In createObservable, expose the `listenerId` so that it can be passed to the server

4. Do the same for the Kotlin client by returning an extension of the `Flow` interface 

5. Test for both client and server

6. Update AutoTable server impl to support participants 

7. Update CRUD calls to pass a listenerId as a participant

8. Pass the listenerId of the table when doing edit operations:

   ``` 
   const listener = listen(() => {...})
   onClick(() => {
   	server.edit(editParams, listener.id)
   })
   ```

   

# 2. Low Priority - Do later
### Reduce error duplication when a type is not serializable.
Currently we get two errors - one for the method having a non-serializable param, and then one for the type itself not being serializable. 
TBH I think we can get rid of the method check because the type check encompasses it. 

### Bind gradle version to a specific typescript version
We need to avoid cases where you for example have gradle version that is meant for typescript 0.5, but 0.6 has been released and breaks everything.
Currently, it just downloads the latest, but it should be bound to a specific version. 

### Improve server testing with "in-memory-server" client generation 
For every service, in addition to generating a client that interacts with the server from network, there should also be a client that
interacts with an actual server instance in-memory. This is because we no longer override the service class in the generated client,
so we need a unified interface for testing.
For this to work, we will need to create four new generated classes:
A. The client interface.
B. The in-memory client.
C. The invoker interface
D. The in-memory invoker. 

Additionally, all @Api classes must implement `RPC` to expose their `invoker`. 

Then the network client will implement the client interface, and the new in-memory client will also implement the same client interface.
This way the same code could be used to test both implementations.

I've already started implementing it but decided for a simpler approach that works but is less efficient. See testApp.ManualMemoryClient and processor.ApiDefinitionToClientInterface


### Support optional properties

Kotlin properties with a default value should be considered optional:

```kotlin
class SomeData(val y: Int = 3)
```

The parameter `x` should have `isOptional` set to `true` in the `RpcParameter` , and the property `y` should have `optional` set to `true` in the `RpcModel.Struct.Property`.

~~Currently we can't know if something has a default value because ksp doesn't expose it (for properties at least). With a compiler plugin it should be simple to determine something has a default value.~~ I think this is possible by accessing the primaryConstructor.

Typescript should interpret optional parameter and properties with the `?` property/parameter operator. In json, we can simply omit optional properties (or specify `undefined` in javascript)

We this is supported we can allow not encoding defaults in Kotlinx.serialization formats.

1. Test optional RPC parameters, and omitting values in various positions in the parameter list
2. Test optional data class properties

The main problem with this is that there's no real good way to represent in clients objects that have optional properties. 
On the one hand, the server promises that it will always give the property a value - at least the default value, which means the property should not be nullable.
On the other hand, the client may construct the object without specifying the optional property value, which means the property should be nullable. 

One reasonable way to deal with this is to accept that the value will be nullable on the client:
```kotlin
data class Foo(val x: Int = 2)
```
Becomes
```typescript
interface Foo {
    x?: number
}
```

However, here is maybe a more apt solution: 
```kotlin
data class Foo(val x: Int = 2)
```
Becomes

```typescript
class Foo {
    _x: number | undefined
    get x(): number {
        GeneratedCodeUtils.checkDefined(this._x, "x", this)
        return this._x!
    }

    constructor({x}: { x?: number }) {
        this._x = x
    }
}

namespace GeneratedCodeUtils {
    import Instance = WebAssembly.Instance;

    function checkDefined(value: unknown, name: string, instance: object) {
        if (value === undefined) {
            const className = instance.constructor.name
            throw new Error(`The property '${name}' was accessed on an instance of ${className} that was given no value.
            When creating an instance ${className} without specifying a value to '${name}', the instance should be passed directly to the server as it knows what the default value is.
            If you are creating an instance of ${className} yourself and want to access '${name}' later, you must give it an actual value. 
            Here is the problematic ${className} instance: ${JSON.stringify(Instance)}`)
        }
    }
}
```

The main issue is that we need to rework our deserialization mechanism to support classes. 


### Generate Doc Comments

Given a server method:

```kotlin
/**
* Does something great with [param1]. It's almost as good as the [second parameter][param2]. 
* Use the class [MyDataClass] for this. 
* Learn more [here](https://www.google.com/).
* @param param2 The second parameter
* @return 2 if the sky is blue, 1 otherwise
*/
fun doSomething(param1: String, param2: Int): Int
```

The generated Typescript file should have it transformed:

```typescript
/**
* Does something great with {@link param1}. It's almost as good as the {@link param2|second parameter}. 
* Use the class {@link MyDataClass} for this. 
* Learn more {@link https://www.google.com/|here}.
* @param param2 The second parameter
* @return 2 if the sky is blue, 1 otherwise
*/
function doSomething(param1: string, param2: number): number
```

This requires a model like this for comments:

```kotlin
data class Documentation(val body: FormatString, val parameters: Map<String, FormatString>, val returnType: FormatString)
data class FormatString(val string: String, val arguments: List<Reference>)
data class Reference(val visibleText: String?, val link: String, val kind: ReferenceKind)
enum class ReferenceKind {
    Value, Type, URL
}
```

Where a string of a `FormatString` looks like this: 

```
Does something great with ${}. It's almost as good as the ${}. 
Use the class ${} for this. 
Learn more ${}.
```

### Build complete Ktor-based API

We should support the following use cases:

- Completely managed. This is how Crashy does it, and in this case we spin up a full ktor server for you. Everything regarding the ktor server should be configurable to support all features, but a barebones server should be a one-liner. 
- Single route. This is how Loggy does it. Given a ktor `Routing` context, we should add a function to register an RPC server for the given route string. 

### Build complete Api-Gateway based API

To support the caesarea POC, we should interface with the ApiGateway api to allow setting up a RPC server in an AWS Lambda. 

### Wrap all configuration of a generated class with a `RpcConfig`:

```kotlin
class MyApiImpl(val config: RpcConfig)
```

And then pass that instead to `GenerateCodeUtils`, to make it easier to add new fields to the config - we won't need to change the generated code. 




### Allow call contexts

In servers using Loggy, we rely on a a `LoggingContext` to group up all logs of a single call. This is still doable with the current approach, but it requires manually construction a Loggy instance for every RPC definition. We should allow specifying a context for a RPC:

```kotlin
context(SomeContext)
fun someCall() {
   // logic...   
}
```

And then allow constructing a `SomeContext` for each call:

```kotlin
val config : RpcConfig = RpcConfig {
    contextProvider { methodName, client, ...
        SomeContext(methodName, client, ...)
    }
}
```

And the context will be made available for every call. 

The generated kotlin client code shouldn't include this context. 


### Allow mocking the server on clients
Say you have a server like so:
```kotlin
@Api class MyService {
    fun doSomething() {
        // ...
    }
}
```

On the clients, this will be generated:
```typescript
class MyServiceApi {
    doSomething(request: RobotAction): Promise<void> {
        return // ...
    }
}
```

I need to investigate if I can somehow mock the doSomething call with something local (implemented by ts client manually), to be able to run server functions without a real server. 

### Remove unused code and cleanup

There's a bunch of leftover stuff, I should exterminate it and maybe do some restructuring in the files. 

### Rethink the way we type things
There's something not consistent with the way we type things, including interfacing with KSP and KotlinPoet.

### Enable public api mode

Add the kotlin compiler flag that forces everything to be public or internal, and figure out which needs to be which.

### Document API

Every `public` function or class should have detailed javadocs explaining it. 

### Lower visibility of debug prints
warn -> info, info -> debug, etc. 


### Publish with CD
Publish with github actions. 


# 3a. The compiler plugin
# 3. Blocked - Requires compiler plugin
### Remove "packageName" from the RpcType
RPC4a prohibits identical names for models for simplicity. This means there's no need to specify a "package name" for types, and the simple name is enough.
Currently the only reason we specify a packageName is that Kotlinx.serialization demands the fully qualified name of the subclasses of polymorphic types
to deserialize them. When we have the package name, it's easy to specify the fully qualified name in a type adapter.  
 Possible alternatives:
1. Annotate every single sealed subclass with @SerialName("SimpleName") - too much work for the user.
2. Register completely custom serializers that customize the descriptor of every sealed subclass to have the serialName be equal
    to the simple name. This is possible but is a ton of work, in the SerialModule, we need to register every sealed class,
    and for each sealed class we need to register each subclass, and for each subclass we need to register the custom serializer
    and then that doesn't work for top-level serialization so we need to create custom Serializers for every single generic serializer to use
    our special simple subclass serializers. Fuck that.
3. Fork kotlinx.serialization to use simple names instead of qualified names - has the usual problems of forking.
4. Use a compiler plugin to generate @SerialNames with simple names for every class <---- this is probably the best solution, we will do it
    once this becomes a compiler plugin.
5. Allow simple serial names with a kotlinx.serialization PR, see https://github.com/Kotlin/kotlinx.serialization/issues/2319#issuecomment-1771023838

### Compact RpcType generation in Typescript

We specify `RpcType`s in Typescript to provide type information for type adapters. The problem is that it becomes very large:

```json
[{name:"bool",packageName:"kotlin"}, {name:"i8",packageName:"kotlin"}, {name:"i16",packageName:"kotlin"}, ...
```

A big reason for how big it is is package names, which we will get rid of.

Considering how important bundle size is in frontend, we should take the following approach to compact this information:

- When only `name` is present, represent the object as a `string` of the name only

- Otherwise, represent the object as an array where the elements are: `name`, `typeArguments`, `isNullable`, `isTypeParameter`, `inlinedType` in that order, where everything starting from `isNullable` is optional. In total:

  ```typescript
  type CompactRpcType = string | [string, string, boolean?, boolean?, RpcType?]
  ```

### Force serialization of Pair, Triple, Map.Entry, and Unit with our own serializer in data classes
These classes use a custom serializer to fit with the Rpc4a standard. However, unless we specify @Contextual, these types in @Serializable classes
will serialize using the builtin serializers. Right now we simply require @Contextual to be used, but with a compiler plugin we should force 
serialization with our specific serializer using @Serializable (with = OurSerializer()) on every property of these types. 

# 4. Nice to have - Will be done much later

### Attempt to simplify  the `RpcServerEngine` interface

That interface is currently very complicated. It may be possible to make it into something much simpler and integrate unique extension methods for every engine so that it remains concise to use them. 

It may best to have utility methods for the common cases, then for each engine to have an extension method that is the entrypoint.

### Add Reflection helpers for generated classes
For example, when doing:

```kotlin
   RpcServerSetup.managedKtor(BasicApi(), BasicApi.server()).createServer().start(wait = true)
```

It can be shorted with reflection to: 
```kotlin
   RpcServerSetup.managedKtor(BasicApi()).createServer().start(wait = true)
```

### Deal with sealed inline classes
This is currently bugged, see:
https://github.com/Kotlin/kotlinx.serialization/issues/2374

### Respect @SerialName for code generation
You should be able to use @SerialName on classes to force code generation with a certain struct name.
Make sure that it works with polymorphic types.
Also, you should be able to use @SerialName on fields to force specific struct field names.

One of the main complications is the fact that if we say that a class named X will be 'serial named' Y, the resulting api definition
will say that the class is named Y. While this is fine for typescript, in generated Kotlin code it will try to reference Y, 
but only X actually exists in Kotlin-land. In order for this to work properly we need to separate between the representation 
that generated clients use and the representation that kotlin uses. 

### Modularize RPC4All
RPC4k interfaces with many foreign libraries that are not required for the core logic:
- Ktor
- OkHttp
- Json Format
- JUnit
RPC4K should have separate modules for each dependency, to prevent pulling in unnecessary dependencies.  

Additionally:

- Split the KSP and the runtime aritfacts.
- Split the typescript generator and runtime library. 



### Support complex keys in Typescript
The following serializes fine:
```kotlin
data class Foo(val x: Int, val y: String)
typealias MyMap = Map<String, Foo>
```
But this doesn't work:
```kotlin
typealias MyMap = Map<Foo, Foo>
```

The reason is that JSON doesn't natively support non-primitive map keys. 
However, Kotlin works around this by serializing that object like so:

```kotlin
mapOf(Foo(x = 1, y = "2") to Foo(x = 3, y = "4"))
```
Becomes
```json
[
 {"x":1,"y":"2"},
 {"x":3,"y":"4"}
]
```

1. Typescript doesn't support complex map keys, so these classes should instead use my api of Dict (fudge-lib) as the type.  
2. the JS -> RPC type adapter should be extended to detect complex map key types, and convert the array of key-values into a HashMap (which implements Dict) accordingly.  
3. The RPC -> JS type adapter should be extended to simply serialize HashMaps as an array of key-values.  
4. Update AlignWithType to handle Hashmaps 

### Generate Kotlin clients from non-kotlin servers
Currently we support a Kotlin client only if the server is in kotlin and in the same compilation context. We should support Kotlin model
codegen like in Typescript to allow kotlin clients to interface with things like C++ servers. 

### Route using HTTP routing

Currently, to support all kinds of servers, we include the route as part of the payload, which has some cost and complexity attached. Optimally, if routing is available like in ktor, we should use the builtin support for routing and drop the custom RPC header. 

This would require defining an interface for servers that support this:

```kotlin
interface RoutingRpcServer {
    // Something along these lines:
	fun route(path: String, (body: ByteArray) -> Unit)
}
```

Additionally, the generated RPC definition json should include a flag that says that http routing is available. I think having something like this makes sense:

```typescript
type Routing = "http" | "rpc4a" | ... // Other methods of routing 
```

And then the clients will know to use the normal http routing instead. 

### Support WebSocket

Websocket is a problem. There's no definitive way to know what the "response" to a request is.
However, this can be solved by providing a RequestId in each request, and then locking until a response with the same RequestId is returned.

### Support optional parameters on RPCs

Kotlin parameters in RPCs with a default value should be considered optional:

```kotlin
fun someFunc(y: Int = 3)
```

The client may then omit the value, and the value `3` will be used instead.
Implementing this is quite complicated, because Kotlin doesn't support something of the form 'if X, use this value, otherwise, use the default value'.
These are the options to implementing this then:
1. Copy default value verbatim into generated code (requires compiler plugin)
If '3' is defined as the default value, then generated code will look something like this:
someFunc(y ?: 3)
This is a very problematic solution because it requires analyzing actual code and might be very hard to make it work with complex expressions as the default value.
2. Use reflection (JVM-only)
Chatgpt provided this useful example:
```kotlin
fun foo(a: Int = 0, b: Int = 1, c: Int = 2, d: Int = 3, e: Int = 4) {
    println("a: $a, b: $b, c: $c, d: $d, e: $e")
}

fun main() {
    val values: List<Int?> = listOf(9, 8, null, 7, null)

    // Get a reference to the foo function
    val fooFunc: KFunction<Unit> = ::foo

    // Prepare a map of arguments for the function call
    val args = mutableMapOf<KParameter, Any?>()
    fooFunc.parameters.forEachIndexed { index, param ->
        values.getOrNull(index)?.let { value ->
            if (value != null) args[param] = value
        }
    }

    // Call the function with the arguments
    fooFunc.callBy(args)
}
```
I have not tested it, but according to the kdocs this should do what we want. 
3. Generate every possible permutation (exponential generated code size)
Chatgpt a rough outline:
```kotlin
fun callFooWithValues(values: List<Int?>) {
    when {
        // Check each combination of nulls and call foo accordingly
        values[0] != null && values[1] != null && values[2] != null && values[3] != null && values[4] != null -> foo(values[0]!!, values[1]!!, values[2]!!, values[3]!!, values[4]!!)
        values[0] != null && values[1] != null && values[2] != null && values[3] != null -> foo(a = values[0]!!, b = values[1]!!, c = values[2]!!, d = values[3]!!)
        // ... (Other combinations)
        values[0] == null && values[1] == null && values[2] == null && values[3] == null && values[4] != null -> foo(e = values[4]!!)
        // ... (Rest of the combinations)
        else -> foo()
    }
}
```

This requires 2^n of these lines, where n is the number of optional values.


As you can see, these solutions are all problematic. My favorite is to do option 3 and limit the amount of optional parameters. 

### Gradual Feature Adoption

Some features are difficult to implement such as structured map keys, so some mechanism should exist to not require you to support everything.
If the server using a feature forces a client to support it, there should be some warning about an experimental feature. 
If the feature can just not be used by the client, it should be documented that this is optional and there is a simpler way to handle those cases. 

### Improve wrapping of generated Typescript file

Currently wrapping is very rudimentary and pretty bad code by itself. We should improve wrapping across the board to both improve the code generating it and make the resulting generated code actually wrap in all cases. The new approach should all be handled automatically in `CodeBuilder#addLineOfCode` and do some basic parsing to figure out where it is okay to wrap. 

### Document internals & refactor

Once we are pretty confident of the implementation, we should go around the code explaining the architecture, to make the code easier to deal with. I should go over every file and see if anything is unclear, and maybe do some refactoring

### Support public API mode

For use cases involving exposing an API to many users, we should generate an endpoint for RPC servers that returns the API Definition json. Then, in clients we should support specifying some definition URL, and the client generator will fetch the definition from there and generate the necessary code. 

### Support setting the format on an individual request

This would allow the server to support many formats while the client can choose whichever format it prefers or supports better. Note that HTTP already supports this as a header, so we don't need to add it to the RPC format for a simple HTTP request. 

### Split generated javascript sources to .d.ts and .js
This would make it easier to look at the code for users. 

### Allow multiple source sets for a kotlin source
Currently, we only allow using the `main` source set. We should allow rpc4k to work with an arbitrary amount of source sets.
This requires work in the gradle plugin - apply `ksp` configuration on multiple source sets that are relevant, and generate typescript
generation tasks for each source set separately. 

### Graph QL-like calls
It should be possible to expand the protocol to allow complex calls that for example retrieve some object that contains an id,
then use that id to make another request, without doing any extra round-trips. 

### Allow splitting rpc server definitions across multiple classes/files

@Api classes can get big and unwieldy, it should be possible to split them. 

### Think of a way to simplify the "simple event" pattern:
These types of events may be very common:
```kotlin
@RpcEvent fun foo(@Dispatch value: SomeType): SomeType {
    return value
}
```

# 5. Alpha

Once all of the above tasks are done, I should release an official alpha for the library. 

### Write RPC4All spec
Write a full document detailing everything that RPC4All expects in its networking format and its code generation format 

### Review API
Go over all APIs and see if they are in the correct location and make sense. 

### Write documentation

I need to figure out how I want to host the documentation, and write it. There should be detailed examples, how-to's, getting started, etc. 

### Create landing page

Find a nice and easy way to create a landing page that highlights the main features of RPC4All:

- Cross-Language
- Code-First
- Feature-Rich
- Interoperable with existing code
- Supports any format, transport, and environment
- Easy to configure
- Viable both in-house and as a public API

### Create feature comparison table

Compare RPC4All with other frameworks:

- GRPC
- OpenAPI
- Thrift
- Etc

Across various metrics:

- Code-First?
- Supports Generics?
- Any Format?
- Any other advantage that RPC4All has over other approaches

# 6. IDEA Plugin

There are some useful features we can add with an IDEA plugin. 

### Link the source file of the server function to generated client function sources

For example if you have code like this in `../../server/src/main/my/clazz`:

```kotlin
fun someApiFunc()
```

And this generated a typescript function:

```typescript
function someApiFunc()
```

The Typescript function should have an 'overriding' gutter that references the Kotlin function (arrow up button).  
The Kotlin function should have an 'implementors' gutter that references the same function in all clients that have generated code off the api.

Considering this is done with an IDEA plugin, I don't think it requires support from the RPC format. It should be possible to search for a kotlin/typescript class of the appropriate name and attach a reference to it, like how some plugins add "go-to" from json to real classes.  


### Show KSP errors in-editor as compile errors
We should go over all the validations done in the KSP plugin, and do the same validation in an IDEA plugin, and then mark invalid code
with red compiler errors, to improve user experience. 

### Allow cross-language refactoring
 When refactoring a server class name, it should be possible to also execute the same refactoring on the client to update models there. 

# 7. Performance Concerns

### Copying of entire request create Rpc`s

Currently, the read approach is as follows:

1. Read the entire RPC bytes into a ByteArray
2. Copy the body bytes into another ByteArray
3. Deserialize body from the other ByteArray

This requires copying the entire body, which can be quite large. Here is a faster approach, which requires the format to support streams:

1. Read the entire RPC bytes into a ByteArray
2. Create a ByteArrayInputStream out of the ByteArray
3. Read the header
4. Pass the stream into the format to deserialize the body

Note that the same optimization is not possible when writing an `RPC`, because we don't know ahead of time how large the json bytes is going to be. (Which means we won't be able to tell a `ByteArrayOutputStream` what the size of the array should be and would result in copying anyway)

### O(n) search on every object property
When we align an object to its type, we iterate through every property, and get the type of that property to align the property to it.
The problem is that we do an O(n) search (n = number of properties in the object) for the property in the type declaration, to know what the type of the object is.  
We could consider having some sort of map from property name to property so that the type of each property can be retrieved in O(1) time.  
I actually think might not be a real concern because for optimal performance I would get rid of type adapters anyway. But this might be worth solving because other solutions are not format agnostic, and we will want to keep type adapters for a format agnostic solution. 

++++ the way we do type adapter in js is very slow, it would be better to have something similar to how kotlin does it. 

### Large size of generated  Typescript code

The generated code is around 27KB, for a not-so-big API. Here are some thing that can reduce the code size

1. Make `GeneratedCodeUtils.request` a total level method like `makeRpc4TsRequest`. This will avoid having to do a complex namespace qualifier for the function calls, and the function name itself should get minified anyway. 

2. Combine the `client`, `format`, and `adapter` fields to a single object and pass it to `makeRpc4TsRequest` to reduce the number of parameters. 

3. Move out all the methods of the generated class into two-level methods that accept the client configuration as the first parameter. This will allow tree shakers to remove all unused functions. We should do some trial and error to see if we need to also split the functions to be in separate files for this to be effective. 

   In addition, this should not always happen because it's only useful when exposing a very large public API to various users. When using a private API, usually all methods will be used so this would not be useful and would just be less nice to user than class methods. For this reason, the generate API Definition should include a flag that says whether this is a public API, and only if true, we should convert everything to top-level methods. Note that providing config options to individual users to change the generated code won't be allowed in any case, because this goes against the design philosophy that the same API should have the same code for all users.  

### Iterating over entire objects for Typescript type adapters is probably slow

Currently, we have a multi-format approach to adapting to and from a simple javascript type - "what JSON.* gives". The problem is that there are probably much faster approaches, for example the one used in Kotlinx.serialization. If we want to do something like what they have there, it's going to be a lot of work and will probably require more work for every single format. 

### Using ISO-Strings for dates is not performant on binary formats
We should have some way to make serialization format specific, so that iso strings are used in json and numbers are used in binary formats like protobuf.

### KSP seems to keep running my processor no matter what
This may get fixed by switching to a compiler plugin. 

# 8. May be supported by Kotlin in the future
### Real union types 
Currently, union types are supported in Kotlin by the virtue of sealed types. This means only objects can be a part of a union type.  
However, [according to Roman Elizarov, Union types are planned](https://youtrack.jetbrains.com/issue/KT-13108/Denotable-union-and-intersection-types#focus=Comments-27-5474923.0-0). So once they are supported, we could adapt them to the full range of possible union types in rpc4a. 

# 10. To be considered
### Unsigned types
Add unsigned types to the spec, like how protobuf has them. 
# 11. Notes
### Error result returning -> Error throwing
Currently in caesarea we use an approach of returning a result object and displaying error GUI if the result is an error. 
We should move to an approach of setting an error boundary for components that do service calls, and then it checks the type of the error
To do an appropriate error message. 