import {CreateLobbyResponse, NestedObject, NestingObject} from "../generated/rpc4ts_AllEncompassingServiceModels";
import {Json} from "../../src/serialization/json/Json";
import {rpc4ts_serializer_CreateLobbyResponse, rpc4ts_serializer_NestingObject} from "../generated/rpc4ts_AllEncompassingServiceSerializers";
import {ArraySerializer} from "../../src/serialization/BuiltinSerializers";

test("Can decode array of generated class", () => {
    const obj = [new CreateLobbyResponse({id: 2})]
    const json = new Json()
    const serializer = new ArraySerializer(rpc4ts_serializer_CreateLobbyResponse())

    expect(json.decodeFromString(serializer, json.encodeToString(serializer,obj))).toEqual(obj)
})
test("Can decode array of nested generated class", () => {
    const obj = [new NestingObject({nested: new NestedObject({x: 2})})]
    const json = new Json()
    const serializer = new ArraySerializer(rpc4ts_serializer_NestingObject())

    expect(json.decodeFromString(serializer, json.encodeToString(serializer,obj))).toEqual(obj)
})