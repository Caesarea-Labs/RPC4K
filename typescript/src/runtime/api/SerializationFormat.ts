export interface SerializationFormat {
    encode(value: unknown): Uint8Array
    decode<T>(raw: Uint8Array): T
}