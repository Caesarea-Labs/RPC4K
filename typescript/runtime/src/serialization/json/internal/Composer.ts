/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {JsonWriter} from "./JsonWriter";
import {Json} from "../Json";

export function createComposer(writer: JsonWriter, json: Json): Composer {
    if (json.configuration.prettyPrint) {
        throw new Error("Not implemented")
        // return new ComposerWithPrettyPrint(writer, json);
    } else {
        return new Composer(writer);
    }
}

export class Composer {
     writer: JsonWriter;
     writingFirst: boolean = true;

    public constructor(writer: JsonWriter) {
        this.writer = writer;
    }

    public indent(): void {
        this.writingFirst = true;
    }

    public unIndent(): void {
        // Implementation if needed
    }

    public nextItem(): void {
        this.writingFirst = false;
    }

    public space(): void {
        // Implementation if needed
    }

    public print(v: string | number | boolean): void {
        if (typeof v === 'number') {
            if (Number.isInteger(v)) {
                // Assuming writer has a method to write long integers
                this.writer.writeNumber(v);
            } else {
                // For floating-point numbers
                this.writer.write(v.toString());
            }
        } else if (typeof v === 'boolean') {
            this.writer.write(v.toString());
        } else {
            // For string and char
            this.writer.write(v);
        }
    }

    public printQuoted(value: string): void {
        // Assuming writer has a method to write quoted strings
        this.writer.writeQuoted(value);
    }
}