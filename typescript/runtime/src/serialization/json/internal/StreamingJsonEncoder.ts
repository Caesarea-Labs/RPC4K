/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {AbstractEncoder} from "../../core/encoding/AbstractEncoder";
import {JsonEncoder} from "./JsonEncoder";
import {Json} from "../Json";
import {Composer, createComposer} from "./Composer";
import {JsonWriter} from "./JsonWriter";
import {switchMode, WriteMode, WriteModes} from "./WriteMode";
import {SerialDescriptor} from "../../core/SerialDescriptor";
import {SerializationStrategy} from "../../core/TsSerializer";
import {encodePolymorphically} from "./Polymorphic";
import {Encoder} from "../../core/encoding/Encoder";
import {INVALID, NULL} from "./AbstractJsonLexer";
import {CompositeEncoder} from "../../core/encoding/Encoding";
import {InvalidFloatingPointEncoded} from "./JsonExceptions";
import {getJsonElementName} from "./JsonNamesMap";

export class StreamingJsonEncoder extends AbstractEncoder implements JsonEncoder {
    private composer: Composer;
    public json: Json;
    private mode: WriteMode;
    private modeReuseCache: Array<JsonEncoder | null> | null;
    public serializersModule: SerializersModule;
    private configuration: any; // Assuming configuration is of a certain type
    private forceQuoting: boolean = false;
    private polymorphicDiscriminator: string | null = null;

    public constructor(
        composer: Composer,
        json: Json,
        mode: WriteMode,
        modeReuseCache: Array<JsonEncoder | null> | null
    ) {
        super();
        this.composer = composer;
        this.json = json;
        this.mode = mode;
        this.modeReuseCache = modeReuseCache;
        this.serializersModule = json.serializersModule;
        this.configuration = json.configuration;

        const i = mode.ordinal
        if (modeReuseCache !== null) {
            if (modeReuseCache[i] !== null && modeReuseCache[i] !== this) {
                modeReuseCache[i] = this;
            }
        }
    }

    public static internalConstructor(
        output: JsonWriter,
        json: Json,
        mode: WriteMode,
        modeReuseCache: Array<JsonEncoder | null> | null
    ): StreamingJsonEncoder {
        const composer = createComposer(output, json);
        return new StreamingJsonEncoder(composer, json, mode, modeReuseCache);
    }


    shouldEncodeElementDefault(descriptor: SerialDescriptor, index: number): boolean {
        return this.configuration.encodeDefaults;
    }

    encodeSerializableValue<T>(serializer: SerializationStrategy<T>, value: T): void {
        encodePolymorphically(this, serializer, value, (discriminator: string) => {
            this.polymorphicDiscriminator = discriminator;
        });
    }


    private encodeTypeInfo(descriptor: SerialDescriptor): void {
        this.composer.nextItem();
        this.encodeString(this.polymorphicDiscriminator!);
        this.composer.print(':');
        this.composer.space();
        this.encodeString(descriptor.serialName);
    }

    public beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        const newMode = switchMode(this.json, descriptor)
        if ( newMode.begin !== INVALID) { // Assuming INVALID is defined
            this.composer.print(newMode.begin);
            this.composer.indent();
        }

        if (this.polymorphicDiscriminator !== null) {
            this.encodeTypeInfo(descriptor);
            this.polymorphicDiscriminator = null;
        }

        if (this.mode === newMode) {
            return this;
        }

        return this.modeReuseCache?.[newMode.ordinal] ?? new StreamingJsonEncoder(this.composer, this.json, newMode, this.modeReuseCache);
    }

    public endStructure(descriptor: SerialDescriptor): void {
        if (this.mode.end !== INVALID) {
            this.composer.unIndent();
            this.composer.nextItem();
            this.composer.print(this.mode.end);
        }
    }

    public encodeElement(descriptor: SerialDescriptor, index: number): boolean {
        switch (this.mode) {
            case WriteModes.LIST:
                if (!this.composer.writingFirst)
                    this.composer.print(',');
                this.composer.nextItem();
                break;
            case WriteModes.MAP:
                if (!this.composer.writingFirst) {
                    this.forceQuoting = index % 2 === 0;
                    if (this.forceQuoting) {
                        this.composer.print(',');
                        this.composer.nextItem(); // indent should only be put after commas in map
                    } else {
                        this.composer.print(':');
                        this.composer.space();
                    }
                } else {
                    this.forceQuoting = true;
                    this.composer.nextItem();
                }
                break;
            case WriteModes.POLY_OBJ:
                if (index === 0)
                    this.forceQuoting = true;
                if (index === 1) {
                    this.composer.print(',');
                    this.composer.space();
                    this.forceQuoting = false;
                }
                break;
            default:
                if (!this.composer.writingFirst)
                    this.composer.print(',');
                this.composer.nextItem();
                this.encodeString(getJsonElementName(descriptor,this.json,index)); // Assuming getJsonElementName method
                this.composer.print(':');
                this.composer.space();
                break;
        }
        return true;
    }

    public encodeNullableSerializableElement<T>(
        descriptor: SerialDescriptor,
        index: number,
        serializer: SerializationStrategy<T>,
        value: T | null
    ): void {
        if (value !== null || this.configuration.explicitNulls) {
            super.encodeNullableSerializableElement(descriptor, index, serializer, value);
        }
    }

    public encodeInline(descriptor: SerialDescriptor): Encoder {
        // if (descriptor.isUnsignedNumber) { // Assuming isUnsignedNumber is a property of SerialDescriptor
        //     return new StreamingJsonEncoder(this.composerAs(ComposerForUnsignedNumbers), this.json, this.mode, null); // Assuming ComposerForUnsignedNumbers is defined
        // } else if (descriptor.isUnquotedLiteral) { // Assuming isUnquotedLiteral is a property of SerialDescriptor
        //     return new StreamingJsonEncoder(this.composerAs(ComposerForUnquotedLiterals), this.json, this.mode, null); // Assuming ComposerForUnquotedLiterals is defined
        // } else {
            return super.encodeInline(descriptor);
        // }
    }

    // private composerAs<T extends Composer>(composerCreator: (writer: JsonWriter, forceQuoting: boolean) => T): T {
    //     if (this.composer instanceof T) {
    //         return this.composer;
    //     } else {
    //         return composerCreator(this.composer.writer, this.forceQuoting);
    //     }
    // }

    encodeNull(): void {
        this.composer.print(NULL);
    }

    encodeBoolean(value: boolean): void {
        if (this.forceQuoting) this.encodeString(value.toString());
        else this.composer.print(value);
    }

    encodeNumber(value: number): void {
        if (this.forceQuoting) this.encodeString(value.toString());
        else this.composer.print(value);
    }

    encodeString(value: string): void {
        this.composer.printQuoted(value);
    }

    encodeEnum(enumDescriptor: SerialDescriptor, index: number): void {
        this.encodeString(enumDescriptor.getElementName(index));
    }
}

export function isUnsignedNumber(descriptor: SerialDescriptor): boolean {
    return false
    // TODO
    // return     descriptor.isInline && descriptor in unsignedNumberDescriptors
}