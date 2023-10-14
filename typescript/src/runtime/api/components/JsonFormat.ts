import {SerializationFormat} from "../SerializationFormat";

//TODO: this won't work well with maps with non-primitive keys
export const JsonFormat: SerializationFormat = {
    decode<T>(raw: Uint8Array): T {
        return JSON.parse(new TextDecoder().decode(raw)) as T
    },
    encode(value: unknown): Uint8Array {
        return new TextEncoder().encode(JSON.stringify(value))
    }
}