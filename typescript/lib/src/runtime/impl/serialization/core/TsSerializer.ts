import {SerialDescriptor} from "./SerialDescriptor";
import {Encoder} from "./Encoder";
import {Decoder} from "./Decoder";

/**
 * TSerializer is responsible for the representation of a serial form of a type T
 * in terms of encoders and decoders and for constructing and deconstructing T
 * from/to a sequence of encoding primitives.
 *
 * ... (rest of the comments)
 */
export interface TsSerializer<T> extends SerializationStrategy<T>, DeserializationStrategy<T> {
    descriptor: SerialDescriptor;
}

/**
 * Serialization strategy defines the serial form of a type T, including its structural description,
 * declared by the descriptor and the actual serialization process, defined by the implementation
 * of the serialize method.
 *
 * ... (rest of the comments)
 */
export interface SerializationStrategy<T> {
    descriptor: SerialDescriptor;
    serialize(encoder: Encoder, value: T): void;
}

/**
 * Deserialization strategy defines the serial form of a type T, including its structural description,
 * declared by the descriptor and the actual deserialization process, defined by the implementation
 * of the deserialize method.
 *
 * ... (rest of the comments)
 */
export interface DeserializationStrategy<T> {
    descriptor: SerialDescriptor;
    deserialize(decoder: Decoder): T;
}