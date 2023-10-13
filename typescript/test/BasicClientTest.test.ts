import {RpcClient} from "../src/runtime/api/RpcClient";
import {SerializationFormat} from "../src/runtime/api/SerializationFormat";
import {GeneratedCodeUtils} from "../src/runtime/implementation/GeneratedCodeUtils";
import {FetchRpcClient} from "../src/runtime/api/components/FetchRpcClient";
import {JsonFormat} from "../src/runtime/api/components/JsonFormat";

test("Basic Client works", () => {
    const client = new MyApiClientImpl(new FetchRpcClient("http://localhost:8080"), JsonFormat)
    const dog = {name: "asdf", type: "shiba", age: 2}
    client.putDog(dog)
    const dogs = client.getDogs(2, "shiba")
    expect(dogs).toContain(dog)
})

class MyApiClientImpl {
    private readonly client: RpcClient
    private readonly format: SerializationFormat

    constructor(client: RpcClient, format: SerializationFormat) {
        this.client = client
        this.format = format
    }

    getDogs(num: number, type: string): Dog[] {
        return GeneratedCodeUtils.request(this.client, this.format, "getDogs", num, type)
    }

    putDog(dog: Dog) {
        return GeneratedCodeUtils.request(this.client, this.format, "putDog")
    }
}


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