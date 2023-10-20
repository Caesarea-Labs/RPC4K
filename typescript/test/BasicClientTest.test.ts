import {FetchRpcClient} from "../src/runtime/components/FetchRpcClient";
import {JsonFormat} from "../src/runtime/components/JsonFormat";
import {RpcClient} from "../src/runtime/RpcClient";
import {SerializationFormat} from "../src/runtime/SerializationFormat";
import {GeneratedCodeUtils} from "../src/runtime/impl/GeneratedCodeUtils";




//TODO: for union types, generate classes instead with isX checks


//TODO: note that this is different from the current approach i have of explicitly handling fetch and generic errors...
// what i should do instead is have the error boundary handle everything but have it change the display based on the exception,
// it could differ between different RpcExceptions


interface Dog {
    name: string
    type: string
    age: number
}

