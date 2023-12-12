import {SerializationFormat} from "../SerializationFormat";
import {DeserializationStrategy, SerializationStrategy} from "../serialization/TsSerializer";
import {Json} from "../serialization/json/Json";

const json = new Json()
export const JsonFormat: SerializationFormat = {
    decode<T>(deserializer: DeserializationStrategy<T>, raw: Uint8Array): T {
        return json.decodeFromString(deserializer, new TextDecoder().decode(raw))
    },
    encode<T>(serializer: SerializationStrategy<T>, value: T): Uint8Array {
        return new TextEncoder().encode(json.encodeToString(serializer, value))
    }
}