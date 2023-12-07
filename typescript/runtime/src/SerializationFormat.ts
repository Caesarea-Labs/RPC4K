import {DeserializationStrategy, SerializationStrategy} from "./serialization/TsSerializer";

export interface SerializationFormat {
    encode<T>(serializer: SerializationStrategy<T>, value: T): Uint8Array
    decode<T>(deserializer: DeserializationStrategy<T>, raw: Uint8Array): T
}