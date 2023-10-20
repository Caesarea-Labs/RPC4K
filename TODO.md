# 1. High Priority - Do now

### Ban "type" as a property name for children of sealed classes

In classes that extend sealed types, disallow `type` as a property name in the KSP validator as this conflicts with the type discriminator

### Test behavior of the server returning an enum with values to a typescript client

If I have an enum like this:

```kotlin
enum class Foo(val x: Int) {
    Bar(2)
}
```

It would be interpreted in typescript like this:

```typescript
type Foo = "Bar"
```

And would probably fail to go from kotlin to typescript if the server returned it. I think I will choose to convert `Foo` to a string in typescript, and assume the client doesn't need the value of `x`. In the future if this causes me an issue I could consider changing it. One reason for converting directly to string instead of a union of structs is that it should not be possible to assign other values to the value of `x` in this example. 

### Support dates 
Instant should be serialized into dayjs instances and vice versa. The date value is an ISO string. 
1. Add a date type to the spec of RPC4all: 
   - Output the date type for Instant and ZonedDateTime in Kotlin
   - Convert date to dayjs in the generated types
2. Add an InstantSerializer() and a ZonedDateTimeSerializer() and add it to the default module
3. Add those two to the list of available builtin serializers and generate them when used as args/return types. 
4. Add to the RPC -> JS adapter an adapter from date typed strings to dayjs (dayjs->ISO string is the default behavior)
# 2. Low Priority - Do later

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

  

# 4. Nice to have - Will be done much later
### Modularize RPC4k
RPC4k interfaces with many foreign libraries that are not required for the core logic:
- Ktor
- OkHttp
- Json Format
RPC4K should have separate modules for each dependency, to prevent pulling in unnecessary dependencies.  
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

# 5. IDEA Plugin

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

# 6. Performance Concerns
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

# 7. May be supported by Kotlin in the future
### Real union types 
Currently, union types are supported in Kotlin by the virtue of sealed types. This means only objects can be a part of a union type.  
However, [according to Roman Elizarov, Union types are planned](https://youtrack.jetbrains.com/issue/KT-13108/Denotable-union-and-intersection-types#focus=Comments-27-5474923.0-0). So once they are supported, we could adapt them to the full range of possible union types in rpc4a. 

///////////////////

NiceToHave: Have a mechanism of gradual feature adoption. 
Some features are difficult to implement such as structured map keys, so some mechanism should exist to not require you to support everything.
If the server using a feature forces a client to support it, there should be some warning about an experimental feature. 
If the feature can just not be used by the client, it should be documented that this is optional and there is a simpler way to handle those cases. 



Performance: typescript typeadapter approach is slow

NiceToHave: non-dayjs date types

NiceToHave: wrap the generated typescript file better

LowPriority: Go through the database and remove random code that is unused
LowPriority: Go through the API and document everything
NiceToHave: Go through the implementation and document everything
NiceToHave: Upload the schema through an API endpoint
HighPriority: Create a standlone npm script that accepts the path of the definition file and the desired output path of the typescript files
and generates stuff. 
HighPriority: Create a gradle script that allows you to specify the root dir of your typescript project and invokes the npm script for you with the relevant paths. 

High Priority: see if todo hightlighting is shared with other users of the project through vcs. 


NiceToHave: support setting the format on an individual request, allowing the server to support many formats while the client can choose whichever
 format it prefers or supports better. Note that HTTP already supports this as a header, so we don't need to add it to the RPC format for simple HTTP request. 