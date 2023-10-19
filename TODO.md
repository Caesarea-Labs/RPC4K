# 1. High Priority - Do now
### Support dates 
Instant should be serialized into dayjs instances and vice versa. The date value is an ISO string. 
1. Add a date type to the spec of RPC4all: 
   - Output the date type for Instant and ZonedDateTime in Kotlin
   - Convert date to dayjs in the generated types
2. Add an InstantSerializer() and a ZonedDateTimeSerializer() and add it to the default module
3. Add those two to the list of available builtin serializers and generate them when used as args/return types. 
4. Add to the RPC -> JS adapter an adapter from date typed strings to dayjs (dayjs->ISO string is the default behavior)
# 2. Low Priority - Do later
# 3a. The compiler plugin
# 3. Blocked - Requires compiler plugin
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

First of all, Typescript doesn't support complex map keys, so these classes should instead use my api of Dict (fudge-lib) as the type.  
Second of all, the JS -> RPC type adapter should be extended to detect complex map key types, and convert the array of key-values into a HashMap (which implements Dict) accordingly.  
Third of all, The RPC -> JS type adapter should be extended to simply serialize HashMaps as an array of key-values.  
# 4. Performance Concerns
### O(n) search on every object property
When we align an object to its type, we iterate through every property, and get the type of that property to align the property to it.
The problem is that we do an O(n) search (n = number of properties in the object) for the property in the type declaration, to know what the type of the object is.  
We could consider having some sort of map from property name to property so that the type of each property can be retrieved in O(1) time.  
I actually think might not be a real concern because for optimal performance I would get rid of type adapters anyway. But this might be worth solving because other solutions are not format agnostic, and we will want to keep type adapters for a format agnostic solution. 
  
++++ the way we do type adapter in js is very slow, it would be better to have something similar to how kotlin does it. 
# 5. May be supported by Kotlin in the future
### Real union types 
Currently, union types are supported in Kotlin by the virtue of sealed types. This means only objects can be a part of a union type.  
However, [according to Roman Elizarov, Union types are planned](https://youtrack.jetbrains.com/issue/KT-13108/Denotable-union-and-intersection-types#focus=Comments-27-5474923.0-0). So once they are supported, we could adapt them to the full range of possible union types in rpc4a. 

///////////////////

NiceToHave: Have a mechanism of gradual feature adoption. 
Some features are difficult to implement such as structured map keys, so some mechanism should exist to not require you to support everything.
If the server using a feature forces a client to support it, there should be some warning about an experimental feature. 
If the feature can just not be used by the client, it should be documented that this is optional and there is a simpler way to handle those cases. 

NiceToHave: non-dayjs date types

LowPriority: Go through the database and remove random code that is unused
LowPriority: Go through the API and document everything
NiceToHave: Go through the implementation and document everything

High Priority: see if todo hightlighting is shared with other users of the project through vcs. 


NiceToHave: support setting the format on an individual request, allowing the server to support many formats while the client can choose whichever
 format it prefers or supports better. Note that HTTP already supports this as a header, so we don't need to add it to the RPC format for simple HTTP request. 