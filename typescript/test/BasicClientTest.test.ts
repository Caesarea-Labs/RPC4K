import {RpcClient} from "../src/runtime/api/RpcClient";
import {SerializationFormat} from "../src/runtime/api/SerializationFormat";
import {GeneratedCodeUtils} from "../src/runtime/implementation/GeneratedCodeUtils";
import {FetchRpcClient} from "../src/runtime/api/components/FetchRpcClient";
import {JsonFormat} from "../src/runtime/api/components/JsonFormat";

test("Basic Client works", async () => {
    const client = new MyApiClientImpl(new FetchRpcClient("http://localhost:8080"), JsonFormat)
    const dog = {name: "asdf", type: "shiba", age: 2}
    await client.putDog(dog)
    const dogs = await client.getDogs(2, "shiba")
    expect(dogs).toContainEqual(dog)
})

class MyApiClientImpl {
    private readonly client: RpcClient
    private readonly format: SerializationFormat

    constructor(client: RpcClient, format: SerializationFormat) {
        this.client = client
        this.format = format
    }

    getDogs(num: number, type: string): Promise<Dog[]> {
        return GeneratedCodeUtils.request(this.client, this.format, "getDogs", num, type)
    }

    putDog(dog: Dog): Promise<void> {
        return GeneratedCodeUtils.request(this.client, this.format, "putDog", dog)
    }
}
//TODO: note that this is different from the current approach i have of explicitly handling fetch and generic errors...
// what i should do instead is have the error boundary handle everything but have it change the display based on the exception,
// it could differ between different RpcExceptions


interface Dog {
    name: string
    type: string
    age: number
}

//class KtorServerTest {
//     companion object {
//
//         @JvmField
//         @RegisterExtension
//         val extension = rpcExtension(MyApi())
//     }
//
//     @Test
//     fun `Basic RPCs work`(): Unit = runBlocking {
//         val client = extension.api
//         val dog = Dog("asdf", "shiba", 2)
//         client.putDog(dog)
//         val dogs = client.getDogs(2, "shiba")
//         expectThat(dogs).isEqualTo(listOf(dog))
//     }
// }
//
// @ApiClient
// @ApiServer
// open class MyApi {
//     companion object;
//     private val dogs = mutableListOf<Dog>()
//     open suspend fun getDogs(num: Int, type: String): List<Dog> {
//         return dogs.filter { it.type == type }.take(num)
//     }
//
//     open suspend fun putDog(dog: Dog) {
//         dogs.add(dog)
//     }
// }
//