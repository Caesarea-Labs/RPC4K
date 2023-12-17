Rpc4ts Codegen Design Challenges

## Requirements

Given a kotlin data class with an API:

```kotlin
sealed interface SomeUnionParent { /**/ }
data class Data(val x: Int, val y: Int = 0, val z: Int?)

interface Api {
    fun getData(): Data
    fun useData(data: Data)
    fun getGeneric(): SomeUnionParent
}
```

An adequate equivalent in Typescript must be generated that supports the following requirements: 
1. **Correct access typing**
Given an instance of `Data`, accessing any properties should retrieve the correct type. 
```typescript
const data = api.getData()
const x : number = data.x
const y: number = data.y // Should be number even though y is optional
const z: number | null = data.z // Should respect nullability
```
2. **Correct construction typing**
When the API accepts `data`, its properties should be able to be specified in the correct way. 
```typescript
api.useData({
    x: 5, // number
    // y: not specified because it's optional
    z: null
})
const x : number = data.x
const y: number = data.y // Should be number even though y is optional
const z: number | null = data.z // Should respect nullability
```
3. **Union type differentiation**
Given an instance of a union type, it should be simple to check what concrete type the instance is.
```typescript
const maybeData = api.getGeneric()
if (isData(maybeData)) {
    // ...
}
```
4. **Copy operator**
An instance of a data class should be easily copyable with new values.
```typescript
const data = api.getData()
const copy = copyModel(data, {x: 4})
```
5. **Runtime type identification for union types**
Given an instance of a union type, it should be simple to retrieve the fully qualified name of the concrete type as string.
```typescript
const data = api.getGeneric()
const type = getRpcName(data) // should be "com.example.Data" for Data 
```

## Solution

For the above Kotlin API, the following Typescript API will be generated.

```typescript
interface Data {
    x: number
    y?: number
    z: number | null
    type: "Data"
}
type SomeUnionParent = Data | ...

class Api {
    getData(): Response<Data>
    useData(data: Data)
    getGeneric(): Response<SomeUnionParent>
}
```

Where `Response<T>` is something like so:

```typescript
export type Response<T> = Promise<NoUndefined<T>>
```

1. **Correct access typing**

`NoUndefined` replaces optional properties with require properties, which would allow accessing `y` on the result with a non-undefined type. 

2. **Correct construction typing**

   `useData` is invokeable like so:

   ```typescript
   api.useData({x: 5, z: null})
   api.useData({x: 5, y: 8, z: null})
   ```

3. **Union type differentiation**

An instance of `SomeUnionParent` may be checked if it's `Data` by testing the `type` field:

```typescript
const maybeData = api.getGeneric()
if (maybeData.type === "data") {
    // maybeData is smart-casted to Data
}
```

4. **Copy operator**

   These models may be simply copied by using object destructuring. 

   ```typescript
   const data = api.getData()
   const copy = {...data, x: 4}
   ```

5. **Runtime type identification for union types**

   In the future, specifying package names won't be required, and the `type` field may be simply use to denote the runtime rpc name. 

   For now, a compile-time generated map will exist that will map the short names to the fully qualified names:

   ```typescript
   const modelFullNames = {
       "Data": "com.example.Data" 
   }
   ```

​	And then fully qualified names by be retrieved like this:

```typescript
export function getRpcName(model: {type: string}): string {
    return modelFullNames[model.type]
}
```


# Rpc4ts Runtime Design challenges
## Requirements:

1.

2.

3.

## Solution

The new solution should probably go back to type adapters and use native solutions for serialization. 
