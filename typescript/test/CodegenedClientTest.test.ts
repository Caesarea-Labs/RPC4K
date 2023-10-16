import {FetchRpcClient} from "../src/runtime/components/FetchRpcClient";
import {JsonFormat} from "../src/runtime/components/JsonFormat";
import {UserProtocolApi} from "./generated/UserProtocolApi";

test("Codegened Client works", async () => {
    const client = new UserProtocolApi(new FetchRpcClient("http://localhost:8080"), JsonFormat)
    const res = await client.createLobby({num: 2}, "asdf")
    expect(res).toEqual({id: 6})
})


//TODO: type: in struct unions
//TODO: in kotlin, serialize map.entry, pair and triple as arrays.