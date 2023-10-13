import {SerializationFormat} from "../SerializationFormat";

export const JsonFormat: SerializationFormat = {
    decode<T>(raw: Uint8Array): T {
        return JSON.parse(new TextDecoder().decode(raw)) as T
    },
    encode(value: unknown): Uint8Array {
        return new TextEncoder().encode(JSON.stringify(value))
    }
}