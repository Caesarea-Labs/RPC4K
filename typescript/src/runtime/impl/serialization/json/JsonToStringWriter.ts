import {JsonWriter} from "./JsonWriter";

/**
 * Simple rewrite of Kotlinx.serialization that just uses string concatenation. Might be very slow.
 */
class JsonToStringWriter implements JsonWriter {
    private array = ""
    private size = 0;

    writeNumber(value: number): void {
        this.write(value.toString());
    }

    writeChar(char: string): void {
        // this.ensureAdditionalCapacity(1);
        this.array += char;
        this.size++;
    }

    write(text: string): void {
        const length = text.length;
        if (length === 0) return;
        // this.ensureAdditionalCapacity(length);
        this.array += text;
        this.size += length;
    }

    writeQuoted(text: string): void {
        // this.ensureAdditionalCapacity(text.length + 2);
        let sz = this.size;
        this.array += '"';
        sz++;
        for (let i = 0; i < text.length; i++) {
            const ch = text.charCodeAt(i);
            if (ch < ESCAPE_MARKERS.length && ESCAPE_MARKERS[ch] !== 0) {
                this.appendStringSlowPath(i, text);
                return;
            } else {
                this.array += text[i];
                sz++;
            }
        }
        this.array += '"';
        this.size = sz + 1;
    }

    private appendStringSlowPath(firstEscapedChar: number, string: string): void {
        for (let i = firstEscapedChar; i < string.length; i++) {
            // this.ensureTotalCapacity(this.size, 2);
            const ch = string.charCodeAt(i);
            if (ch < ESCAPE_MARKERS.length) {
                const marker = ESCAPE_MARKERS[ch];
                switch (marker) {
                    case 0: {
                        this.array += String.fromCharCode(ch);
                        break;
                    }
                    case 1: {
                        const escapedString = ESCAPE_STRINGS[ch];
                        // this.ensureTotalCapacity(this.size, escapedString!.length);
                        this.array += escapedString;
                        this.size += escapedString.length;
                        break;
                    }
                    default: {
                        this.array += '\\' + String.fromCharCode(marker);
                        this.size += 2;
                        break;
                    }
                }
            } else {
                this.array += String.fromCharCode(ch);
            }
        }
        // this.ensureTotalCapacity(this.size, 1);
        this.array += '"';
        this.size++;
    }

    // private ensureAdditionalCapacity(expected: number): void {
    //     this.ensureTotalCapacity(this.size, expected);
    // }
    //
    // private ensureTotalCapacity(oldSize: number, additional: number): void {
    //     const newSize = oldSize + additional;
    //     if (this.array.length <= newSize) {
    //         const newLength = Math.max(newSize, oldSize * 2);
    //         this.array += new Array(newLength - this.array.length).join(' ');
    //     }
    // }
}


const ESCAPE_MARKERS = createEscapeMarkers()





const ESCAPE_STRINGS = createEscapeStrings()

function createEscapeMarkers(): number[] {
    const markers: number[] = new Array(93).fill(0);

    for (let c = 0; c <= 0x1f; c++) {
        markers[c] = 1;
    }
    markers['"'.charCodeAt(0)] = '"'.charCodeAt(0);
    markers['\\'.charCodeAt(0)] = '\\'.charCodeAt(0);
    markers['\t'.charCodeAt(0)] = 't'.charCodeAt(0);
    markers['\b'.charCodeAt(0)] = 'b'.charCodeAt(0);
    markers['\n'.charCodeAt(0)] = 'n'.charCodeAt(0);
    markers['\r'.charCodeAt(0)] = 'r'.charCodeAt(0);
    markers[0x0c] = 'f'.charCodeAt(0);
    return markers
}

function createEscapeStrings(): string[] {
    const escape: (string | null)[] = Array(93).fill(null).map((_, c) => {
        if (c <= 0x1f) {
            const c1 = toHexChar(c >> 12);
            const c2 = toHexChar(c >> 8);
            const c3 = toHexChar(c >> 4);
            const c4 = toHexChar(c);
            return `\\u${c1}${c2}${c3}${c4}`;
        }
        return null;
    });

    escape['"'.charCodeAt(0)] = "\\\"";
    escape['\\'.charCodeAt(0)] = "\\\\";
    escape['\t'.charCodeAt(0)] = "\\t";
    escape['\b'.charCodeAt(0)] = "\\b";
    escape['\n'.charCodeAt(0)] = "\\n";
    escape['\r'.charCodeAt(0)] = "\\r";
    escape[0x0c] = "\\f";

    return escape as string[]
}

const toHexChar = (n: number): string => n.toString(16);