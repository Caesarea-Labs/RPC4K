import {SerializationFormat} from "../SerializationFormat";
import {DeserializationStrategy, SerializationStrategy} from "../serialization/TsSerializer";
import {Json} from "../serialization/json/Json";

const json = new Json()
//TODO: this won't work well with maps with non-primitive keys
export const JsonFormat: SerializationFormat = {
    decode<T>(deserializer: DeserializationStrategy<T>, raw: Uint8Array): T {
        return json.decodeFromString(deserializer, new TextDecoder().decode(raw))
    },
    encode<T>(serializer: SerializationStrategy<T>, value: T): Uint8Array {
        return new TextEncoder().encode(json.encodeToString(serializer, value))
    }
}