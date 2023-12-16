/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {JsonDecoder} from "./JsonDecoder";
import {Json} from "../Json";
import {switchMode, WriteMode, WriteModes} from "./WriteMode";
import {AbstractJsonLexer, COLON, TC_COMMA} from "./AbstractJsonLexer";
import {JsonConfiguration} from "../JsonConfiguration";
import {AbstractDecoder} from "../../core/encoding/AbstractDecoder";
import {ChunkedDecoder} from "../../core/encoding/ChunkedDecoder";
import {SerialDescriptor} from "../../core/SerialDescriptor";
import {DeserializationStrategy} from "../../TsSerializer";
import {JsonElementMarker} from "./JsonElementMarker";
import {CompositeDecoder, Decoder, DECODER_DECODE_DONE, DECODER_UNKNOWN_NAME} from "../../core/encoding/Decoding";
import {isUnsignedNumber} from "./StreamingJsonEncoder";
import {getJsonNameIndex, getJsonNameIndexOrThrow, tryCoerceValue} from "./JsonNamesMap";
import {SerializersModule} from "../../modules/SerializersModule";
import {AbstractPolymorphicSerializer} from "../../internal/AbstractPolymorphicSerializer";
import {classDiscriminator} from "./Polymorphic";

export class StreamingJsonDecoder extends AbstractDecoder implements JsonDecoder, ChunkedDecoder {
    json: Json;
    private mode: WriteMode;
    lexer: AbstractJsonLexer;
    serializersModule: SerializersModule;
    private currentIndex: number = -1;
    private discriminatorHolder: DiscriminatorHolder | null;
    private configuration: JsonConfiguration;
    private elementMarker: JsonElementMarker | null;

    constructor(
        json: Json,
        mode: WriteMode,
        lexer: AbstractJsonLexer,
        descriptor: SerialDescriptor,
        discriminatorHolder: DiscriminatorHolder | null
    ) {
        super();
        this.json = json;
        this.mode = mode;
        this.lexer = lexer;
        this.serializersModule = json.serializersModule;
        this.discriminatorHolder = discriminatorHolder;
        this.configuration = json.configuration;
        this.elementMarker = this.configuration.explicitNulls ? null : new JsonElementMarker(descriptor);
    }

    //
    // decodeJsonElement(): JsonElement {
    //     return new JsonTreeReader(this.json.configuration, this.lexer).read();
    // }

    decodeSerializableValue<T>(deserializer: DeserializationStrategy<T>): T {
        if (!(deserializer instanceof AbstractPolymorphicSerializer) || this.json.configuration.useArrayPolymorphism) {
            return deserializer.deserialize(this);
        }
        const polyDeserializer = deserializer as AbstractPolymorphicSerializer<T>

        const discriminator = classDiscriminator(deserializer.descriptor, this.json)
        const type = this.lexer.peekLeadingMatchingValue(discriminator, this.configuration.isLenient);
        let actualSerializer: DeserializationStrategy<unknown> | null = null;

        if (type !== null) {
            actualSerializer = polyDeserializer.findPolymorphicSerializerOrNullForClassName(this, type);
        }

        if (actualSerializer === null) {
            throw new Error(`Missing 'type' field in polymorphic object of '${deserializer.descriptor.serialName}'`)
            // return this.decodeSerializableValuePolymorphic<T>(deserializer);
        }

        this.discriminatorHolder = new DiscriminatorHolder(discriminator);
        const result = actualSerializer.deserialize(this);
        return result as T;

    }

    beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        const newMode = switchMode(this.json, descriptor)
        this.lexer.path.pushDescriptor(descriptor);
        this.lexer.consumeNextTokenWithExpected(newMode.begin);
        this.checkLeadingComma(); // Assuming this is a method in the class

        if ([WriteModes.LIST, WriteModes.MAP, WriteModes.POLY_OBJ].includes(newMode)) {
            return new StreamingJsonDecoder(
                this.json,
                newMode,
                this.lexer,
                descriptor,
                this.discriminatorHolder
            );
        } else {
            return this.mode === newMode && this.json.configuration.explicitNulls ? this : new StreamingJsonDecoder(this.json, newMode, this.lexer, descriptor, this.discriminatorHolder);
        }
    }

    endStructure(descriptor: SerialDescriptor): void {
        if (this.json.configuration.ignoreUnknownKeys && descriptor.elementsCount === 0) {
            this.skipLeftoverElements(descriptor); // Assuming this is a method in the class
        }

        this.lexer.consumeNextTokenWithExpected(this.mode.end);
        this.lexer.path.popDescriptor();
    }


    private skipLeftoverElements(descriptor: SerialDescriptor): void {
        while (this.decodeElementIndex(descriptor) !== DECODER_DECODE_DONE) {
            // Skip elements
        }
    }

    override decodeNotNullMark(): boolean {
        return !(this.elementMarker?.isUnmarkedNull ?? false) && !this.lexer.tryConsumeNull();
    }

    override decodeNull(): null {
        // Do nothing, null was consumed by `decodeNotNullMark`
        return null;
    }

    private checkLeadingComma(): void {
        if (this.lexer.peekNextToken() === TC_COMMA) {
            this.lexer.failString("Unexpected leading comma");
        }
    }

    override decodeSerializableElement<T>(
        descriptor: SerialDescriptor,
        index: number,
        deserializer: DeserializationStrategy<T>,
        previousValue?: T | null
    ): T {
        const isMapKey = this.mode === WriteModes.MAP && (index & 1) === 0;
        // Reset previous key
        if (isMapKey) {
            this.lexer.path.resetCurrentMapKey();
        }
        // Deserialize the key
        const value = super.decodeSerializableElement(descriptor, index, deserializer, previousValue);
        // Put the key to the path
        if (isMapKey) {
            this.lexer.path.updateCurrentMapKey(value);
        }
        return value;
    }

    override decodeElementIndex(descriptor: SerialDescriptor): number {
        let index: number;
        switch (this.mode) {
            case WriteModes.OBJ:
                index = this.decodeObjectIndex(descriptor);
                break;
            case WriteModes.MAP:
                index = this.decodeMapIndex();
                break;
            default:
                index = this.decodeListIndex(); // Both for LIST and default polymorphic
                break;
        }
        // The element of the next index that will be decoded
        if (this.mode !== WriteModes.MAP) {
            this.lexer.path.updateDescriptorIndex(index);
        }
        return index;
    }

    private decodeMapIndex(): number {
        let hasComma = false;
        const decodingKey = this.currentIndex % 2 !== 0;
        if (decodingKey) {
            if (this.currentIndex !== -1) {
                hasComma = this.lexer.tryConsumeComma();
            }
        } else {
            this.lexer.consumeNextTokenWithExpected(COLON); // Assuming COLON is a predefined constant
        }

        if (this.lexer.canConsumeValue()) {
            if (decodingKey) {
                if (this.currentIndex === -1) {
                    this.lexer.requireCurrentPos(!hasComma, () => "Unexpected trailing comma");
                } else {
                    this.lexer.requireCurrentPos(hasComma, () => "Expected comma after the key-value pair");
                }
            }
            this.currentIndex++;
            return this.currentIndex;
        } else {
            if (hasComma) {
                this.lexer.failString("Expected '}', but had ',' instead");
            }
            return DECODER_DECODE_DONE; // Assuming CompositeDecoder.DECODE_DONE is defined
        }
    }

    private coerceInputValue(descriptor: SerialDescriptor, index: number): boolean { // Replace 'any' with the actual descriptor type
        return tryCoerceValue(this.json,
            descriptor.getElementDescriptor(index),
            () => this.lexer.tryConsumeNull(),
            () => this.lexer.peekString(this.configuration.isLenient),
            () => this.lexer.consumeString() // Skip unknown enum string
        );
    }

    private decodeObjectIndex(descriptor: SerialDescriptor): number { // Replace 'any' with the actual descriptor type
        let hasComma = this.lexer.tryConsumeComma();

        while (this.lexer.canConsumeValue()) {
            hasComma = false;
            const key = this.decodeStringKey(); // Assuming this method is defined
            this.lexer.consumeNextTokenWithExpected(COLON); // Assuming COLON is defined
            const index = getJsonNameIndex(descriptor, this.json, key);
            let isUnknown: boolean;

            if (index !== DECODER_UNKNOWN_NAME) { // Assuming UNKNOWN_NAME is a predefined constant
                if (this.configuration.coerceInputValues && this.coerceInputValue(descriptor, index)) {
                    hasComma = this.lexer.tryConsumeComma(); // Assuming hasComma is defined earlier
                    isUnknown = false; // Known element, but coerced
                } else {
                    if (this.elementMarker !== null) {
                        this.elementMarker.mark(index); // Assuming elementMarker's mark method is defined
                    }
                    return index; // Known element without coercing, return it
                }
            } else {
                isUnknown = true; // Unknown element
            }

            if (isUnknown) {
                hasComma = this.handleUnknown(key); // Assuming this method is defined
            }
        }

        if (hasComma) {
            this.lexer.failString("Unexpected trailing comma");
        }

        return this.elementMarker?.nextUnmarkedIndex() ?? DECODER_DECODE_DONE; // Assuming CompositeDecoder.DECODE_DONE is defined
    }


    private handleUnknown(key: string): boolean {
        if (this.configuration.ignoreUnknownKeys || trySkip(this.discriminatorHolder, key)) {
            this.lexer.skipElement(this.configuration.isLenient);
        } else {
            // Unable to properly update JSON path indices as we do not have a proper SerialDescriptor
            this.lexer.failOnUnknownKey(key);
        }
        return this.lexer.tryConsumeComma();
    }

    private decodeListIndex(): number {
        const hasComma = this.lexer.tryConsumeComma();
        if (this.lexer.canConsumeValue()) {
            if (this.currentIndex !== -1 && !hasComma) {
                this.lexer.failString("Expected end of the array or comma");
            }
            return ++this.currentIndex;
        } else {
            if (hasComma) {
                this.lexer.failString("Unexpected trailing comma");
            }
            return DECODER_DECODE_DONE; // Assuming CompositeDecoder.DECODE_DONE is defined
        }
    }

    public decodeBoolean(): boolean {
        // Prohibit any boolean literal that is not strictly 'true' or 'false', but allow quoted literals in relaxed mode
        return this.configuration.isLenient ? this.lexer.consumeBooleanLenient() : this.lexer.consumeBoolean();
    }

    public decodeNumber(): number {
        const value = this.lexer.consumeStringLenient();
        const asNumber = Number(value.toString())
        // Check for overflow
        if (isNaN(asNumber)) {
            this.lexer.failString(`Failed to parse number for input '${value}'`);
        }
        return asNumber
    }

    private decodeStringKey(): string {
        return this.configuration.isLenient ? this.lexer.consumeStringLenientNotNull() : this.lexer.consumeKeyString();
    }

    public decodeString(): string {
        return this.configuration.isLenient ? this.lexer.consumeStringLenientNotNull() : this.lexer.consumeString();
    }

    public decodeStringChunked(consumeChunk: (chunk: string) => void): void {
        this.lexer.consumeStringChunked(this.configuration.isLenient, consumeChunk);
    }

    public decodeInline(descriptor: SerialDescriptor): Decoder {
        if (isUnsignedNumber(descriptor)) {
            throw new Error("Not implemented")
        }
        return super.decodeInline(descriptor)
    }

    public decodeEnum(enumDescriptor: SerialDescriptor): number { // Replace 'any' with actual enumDescriptor type
        return getJsonNameIndexOrThrow(enumDescriptor, this.json, this.decodeString(), ` at path ${this.lexer.path.getPath()}`); // Adjust based on actual implementation
    }


    // Other methods like decodeSerializableValuePolymorphic, etc., need to be provided.
}

// A mutable reference to the discriminator that have to be skipped when in optimistic phase
// of polymorphic serialization, see `decodeSerializableValue`
class DiscriminatorHolder {
    discriminatorToSkip: string | null

    constructor(discriminatorToSkip: string) {
        this.discriminatorToSkip = discriminatorToSkip
    }

}


function trySkip(discriminatorHolder: DiscriminatorHolder | null, unknownKey: string): boolean {
    if (discriminatorHolder === null) return false
    if (discriminatorHolder.discriminatorToSkip === unknownKey) {
        discriminatorHolder.discriminatorToSkip = null
        return true
    }
    return false
}