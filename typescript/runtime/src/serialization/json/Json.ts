/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {DeserializationStrategy, SerializationStrategy} from "../TsSerializer";
import {JsonToStringWriter} from "./internal/JsonToStringWriter";
import {JsonWriter} from "./internal/JsonWriter";
import {StreamingJsonEncoder} from "./internal/StreamingJsonEncoder";
import {WriteModes, WriteModeValues} from "./internal/WriteMode";
import {JsonConfiguration} from "./JsonConfiguration";
import {StringJsonLexer} from "./internal/StringJsonLexer";
import {StreamingJsonDecoder} from "./internal/StreamingJsonDecoder";
import {EmptySerializersModule, SerializersModule} from "../modules/SerializersModule";

export class Json {
    configuration: JsonConfiguration
    serializersModule: SerializersModule

    constructor(configuration: JsonConfiguration = new JsonConfiguration(), serializersModule: SerializersModule = EmptySerializersModule) {
        this.configuration = configuration
        this.serializersModule = serializersModule
    }

    public encodeToString<T>(serializer: SerializationStrategy<T>, value: T): string {
        const result = new JsonToStringWriter();
        this.encodeByWriter(result, serializer, value);
        return result.toString();
    }
    public decodeFromString<T>(deserializer: DeserializationStrategy<T>, str: string): T {
        const lexer = new StringJsonLexer(str)
        const input = new StreamingJsonDecoder(this, WriteModes.OBJ, lexer, deserializer.descriptor, null)
        const result = input.decodeSerializableValue(deserializer)
        lexer.expectEof()
        return result
    }



    private encodeByWriter<T>(writer: JsonWriter, serializer: SerializationStrategy<T>, value: T): void {
        const encoder = StreamingJsonEncoder.internalConstructor(
            writer, this,
            WriteModes.OBJ,
            new Array(WriteModeValues.length).fill(null)
        );
        encoder.encodeSerializableValue(serializer, value);
    }
}
