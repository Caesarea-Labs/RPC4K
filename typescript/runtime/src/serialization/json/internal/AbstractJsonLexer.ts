/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {JsonPath} from "./JsonPath";
import {createJsonDecodingExceptionWithInput, JsonDecodingException} from "./JsonExceptions";

export const lenientHint = "Use 'isLenient = true' in 'Json {}' builder to accept non-compliant JSON.";
export const coerceInputValuesHint = "Use 'coerceInputValues = true' in 'Json {}' builder to coerce nulls to default values.";
export const specialFlowingValuesHint = "It is possible to deserialize them using 'JsonBuilder.allowSpecialFloatingPointValues = true'";
export const ignoreUnknownKeysHint = "Use 'ignoreUnknownKeys = true' in 'Json {}' builder to ignore unknown keys.";
export const allowStructuredMapKeysHint = "Use 'allowStructuredMapKeys = true' in 'Json {}' builder to convert such maps to [key1, value1, key2, value2,...] arrays.";

// Special strings
export const NULL = "null";

// Special chars
export const COMMA = ',';
export const COLON = ':';
export const BEGIN_OBJ = '{';
export const END_OBJ = '}';
export const BEGIN_LIST = '[';
export const END_LIST = ']';
export const STRING = '"';
export const STRING_ESC = '\\';

export const INVALID = '\u0000'; // 0.toChar() in Kotlin is equivalent to '\u0000' in TypeScript
export const UNICODE_ESC = 'u';

// Token classes
export const TC_OTHER: number = 0;
export const TC_STRING: number = 1;
export const TC_STRING_ESC: number = 2;
export const TC_WHITESPACE: number = 3;
export const TC_COMMA: number = 4;
export const TC_COLON: number = 5;
export const TC_BEGIN_OBJ: number = 6;
export const TC_END_OBJ: number = 7;
export const TC_BEGIN_LIST: number = 8;
export const TC_END_LIST: number = 9;
export const TC_EOF: number = 10;
export const TC_INVALID: number = 0xFF; // Byte.MAX_VALUE in Kotlin is 0xFF in TypeScript

// mapping from chars to token classes
export const CTC_MAX = 0x7e

// mapping from escape chars real chars
export const ESC2C_MAX = 0x75

export const asciiCaseMask = 1 << 5;

function tokenDescription(token: number): string {
    switch (token) {
        case TC_STRING:
            return "quotation mark '\"'";
        case TC_STRING_ESC:
            return "string escape sequence '\\'";
        case TC_COMMA:
            return "comma ','";
        case TC_COLON:
            return "colon ':'";
        case TC_BEGIN_OBJ:
            return "start of the object '{'";
        case TC_END_OBJ:
            return "end of the object '}'";
        case TC_BEGIN_LIST:
            return "start of the array '['";
        case TC_END_LIST:
            return "end of the array ']'";
        case TC_EOF:
            return "end of the input";
        case TC_INVALID:
            return "invalid token";
        default:
            return "valid token"; // should never happen
    }
}

class CharMappings {
    static ESCAPE_2_CHAR: string[] = new Array(ESC2C_MAX).fill('\0');
    static CHAR_TO_TOKEN: number[] = new Array(CTC_MAX).fill(0);

    static initEscape() {
        for (let i = 0; i <= 0x1f; i++) {
            CharMappings.initC2ESC(i, UNICODE_ESC);
        }

        CharMappings.initC2ESC(0x08, 'b');
        CharMappings.initC2ESC(0x09, 't');
        CharMappings.initC2ESC(0x0a, 'n');
        CharMappings.initC2ESC(0x0c, 'f');
        CharMappings.initC2ESC(0x0d, 'r');
        CharMappings.initC2ESCChar('/', '/');
        CharMappings.initC2ESCChar(STRING, STRING);
        CharMappings.initC2ESCChar(STRING_ESC, STRING_ESC);
    }

    static initCharToToken() {
        for (let i = 0; i <= 0x20; i++) {
            CharMappings.initC2TC(i, TC_INVALID);
        }

        CharMappings.initC2TC(0x09, TC_WHITESPACE);
        CharMappings.initC2TC(0x0a, TC_WHITESPACE);
        CharMappings.initC2TC(0x0d, TC_WHITESPACE);
        CharMappings.initC2TC(0x20, TC_WHITESPACE);
        CharMappings.initC2TCChar(COMMA, TC_COMMA);
        CharMappings.initC2TCChar(COLON, TC_COLON);
        CharMappings.initC2TCChar(BEGIN_OBJ, TC_BEGIN_OBJ);
        CharMappings.initC2TCChar(END_OBJ, TC_END_OBJ);
        CharMappings.initC2TCChar(BEGIN_LIST, TC_BEGIN_LIST);
        CharMappings.initC2TCChar(END_LIST, TC_END_LIST);
        CharMappings.initC2TCChar(STRING, TC_STRING);
        CharMappings.initC2TCChar(STRING_ESC, TC_STRING_ESC);
    }

    private static initC2ESC(c: number, esc: string) {
        if (esc !== UNICODE_ESC) CharMappings.ESCAPE_2_CHAR[esc.charCodeAt(0)] = String.fromCharCode(c);
    }

    private static initC2ESCChar(c: string, esc: string) {
        this.initC2ESC(c.charCodeAt(0), esc)
    }

    private static initC2TC(c: number, cl: number) {
        CharMappings.CHAR_TO_TOKEN[c] = cl;
    }

    private static initC2TCChar(c: string, cl: number) {
        this.initC2TC(c.charCodeAt(0), cl)
    }
}

CharMappings.initEscape();
CharMappings.initCharToToken();

export function charToTokenClass(c: string) {
    return c.charCodeAt(0) < CTC_MAX ? CharMappings.CHAR_TO_TOKEN[c.charCodeAt(0)] : TC_OTHER;
}

function escapeToChar(c: number): string {
    return c < ESC2C_MAX ? CharMappings.ESCAPE_2_CHAR[c] : INVALID;
}

/**
 * The base class that reads the JSON from the given char sequence source.
 * It has two implementations: one over the raw string instance, StringJsonLexer,
 * and one over an arbitrary stream of data, ReaderJsonLexer (JVM-only).
 *
 * AbstractJsonLexer contains base implementation for cold or not performance-sensitive
 * methods on top of CharSequence, but StringJsonLexer overrides some
 * of them for the performance reasons (devirtualization of CharSequence and avoid
 * of additional spills).
 */
export abstract class AbstractJsonLexer {
    protected abstract source: string;
    protected currentPosition: number = 0; // position in source
    path: JsonPath = new JsonPath();

    ensureHaveChars(): void {
    }

    isNotEof(): boolean {
        return this.peekNextToken() != TC_EOF; // Assuming TC_EOF is defined elsewhere
    }

    // Used as bound check in loops
    abstract prefetchOrEof(position: number): number;

    abstract tryConsumeComma(): boolean;

    abstract canConsumeValue(): boolean;

    abstract consumeNextToken(): number;

    protected isValidValueStart(c: string): boolean {
        return !['}', ']', ':', ','].includes(c);
    }

    expectEof(): void {
        const nextToken = this.consumeNextToken();
        if (nextToken != TC_EOF)
            this.failString("Expected EOF after parsing, but had " + this.source[this.currentPosition - 1] + " instead");
    }

    private peekedString: string | null = null;
    protected escapedString: string = '';

    consumeNextTokenNumber(expected: number): number {
        const token = this.consumeNextToken();
        if (token != expected) {
            this.fail(expected);
        }
        return token;
    }

    consumeNextTokenWithExpected(expected: string): void {
        this.ensureHaveChars();
        let cpos = this.currentPosition;
        while (true) {
            cpos = this.prefetchOrEof(cpos);
            if (cpos == -1) break;
            const c = this.source[cpos++];
            if ([' ', '\n', '\r', '\t'].includes(c)) continue;
            this.currentPosition = cpos;
            if (c === expected) return;
            this.unexpectedToken(expected);
        }
        this.currentPosition = cpos;
        this.unexpectedToken(expected); // EOF
    }

    protected unexpectedToken(expected: string): void {
        if (this.currentPosition > 0 && expected === STRING) { // Assuming STRING is defined elsewhere
            let inputLiteral: string;
            this.currentPosition--;
            inputLiteral = this.consumeStringLenient();

            if (inputLiteral === NULL) { // Assuming NULL is defined elsewhere
                this.failString("Expected string literal but 'null' literal was found", this.currentPosition - 1, coerceInputValuesHint); // coerceInputValuesHint should be defined
            }
        }
        this.fail(charToTokenClass(expected));
    }

    protected fail(expectedToken: number, wasConsumed: boolean = true): never {
        const expected = tokenDescription(expectedToken); // Assuming tokenDescription is defined elsewhere
        const position = wasConsumed ? this.currentPosition - 1 : this.currentPosition;
        const s = this.currentPosition === this.source.length || position < 0 ? "EOF" : this.source[position];
        throw new Error(`Expected ${expected}, but had '${s}' instead at position ${position}`);
    }

    peekNextToken(): number {
        let cpos = this.currentPosition;
        while (true) {
            cpos = this.prefetchOrEof(cpos);
            if (cpos === -1) break;
            const ch = this.source[cpos];
            if (ch === ' ' || ch === '\n' || ch === '\r' || ch === '\t') {
                ++cpos;
                continue;
            }
            this.currentPosition = cpos;
            return charToTokenClass(ch); // Assuming charToTokenClass is defined elsewhere
        }
        this.currentPosition = cpos;
        return TC_EOF; // Assuming TC_EOF is defined elsewhere
    }

    tryConsumeNull(doConsume: boolean = true): boolean {
        let current = this.skipWhitespaces();
        current = this.prefetchOrEof(current);
        const len = this.source.length - current;
        if (len < 4 || current === -1) return false;
        for (let i = 0; i < 4; i++) {
            if (NULL[i] !== this.source[current + i]) return false; // Assuming NULL is defined elsewhere
        }
        if (len > 4 && charToTokenClass(this.source[current + 4]) === TC_OTHER) return false; // Assuming TC_OTHER is defined elsewhere

        if (doConsume) {
            this.currentPosition = current + 4;
        }
        return true;
    }

    skipWhitespaces(): number {
        let current = this.currentPosition;
        while (true) {
            current = this.prefetchOrEof(current);
            if (current === -1) break;
            const c = this.source[current];
            if (c === ' ' || c === '\n' || c === '\r' || c === '\t') {
                ++current;
            } else {
                break;
            }
        }
        this.currentPosition = current;
        return current;
    }

    abstract peekLeadingMatchingValue(keyToMatch: string, isLenient: boolean): string | null;

    peekString(isLenient: boolean): string | null {
        const token = this.peekNextToken();
        let string: string | null;
        if (isLenient) {
            if (token !== TC_STRING && token !== TC_OTHER) return null; // Assuming TC_STRING and TC_OTHER are defined elsewhere
            string = this.consumeStringLenient();
        } else {
            if (token !== TC_STRING) return null;
            string = this.consumeString();
        }
        this.peekedString = string;
        return string;
    }

    discardPeeked(): void {
        this.peekedString = null;
    }

    indexOf(char: string, startPos: number): number {
        return this.source.indexOf(char, startPos);
    }

    substring(startPos: number, endPos: number): string {
        return this.source.substring(startPos, endPos);
    }

    abstract consumeKeyString(): string;

    protected insideString(isLenient: boolean, char: string): boolean {
        return isLenient ? charToTokenClass(char) === TC_OTHER : char !== STRING; // Assuming charToTokenClass, TC_OTHER, and STRING are defined elsewhere
    }

    consumeStringChunked(isLenient: boolean, consumeChunk: (stringChunk: string) => void): void {
        const nextToken = this.peekNextToken();
        if (isLenient && nextToken !== TC_OTHER) return;

        if (!isLenient) {
            this.consumeNextTokenWithExpected(STRING);
        }
        let currentPosition = this.currentPosition;
        let lastPosition = currentPosition;
        let char = this.source[currentPosition];
        let usedAppend = false;
        while (this.insideString(isLenient, char)) {
            if (!isLenient && char === STRING_ESC) { // Assuming STRING_ESC is defined elsewhere
                usedAppend = true;
                currentPosition = this.prefetchOrEof(this.appendEscape(lastPosition, currentPosition)); // Assuming appendEscape is defined elsewhere
                lastPosition = currentPosition;
            } else {
                currentPosition++;
            }
            if (currentPosition >= this.source.length) {
                this.writeRange(lastPosition, currentPosition, usedAppend, consumeChunk);
                usedAppend = false;
                currentPosition = this.prefetchOrEof(currentPosition);
                if (currentPosition === -1)
                    this.failString("EOF", currentPosition);
                lastPosition = currentPosition;
            }
            char = this.source[currentPosition];
        }
        this.writeRange(lastPosition, currentPosition, usedAppend, consumeChunk);
        this.currentPosition = currentPosition;
        if (!isLenient) {
            this.consumeNextTokenWithExpected(STRING);
        }
    }

    protected writeRange(fromIndex: number, toIndex: number, currentChunkHasEscape: boolean, consumeChunk: (stringChunk: string) => void): void {
        if (currentChunkHasEscape) {
            consumeChunk(this.decodedString(fromIndex, toIndex)); // Assuming decodedString is defined elsewhere
        } else {
            consumeChunk(this.substring(fromIndex, toIndex));
        }
    }

    consumeString(): string {
        if (this.peekedString !== null) {
            return this.takePeeked(); // Assuming takePeeked is defined elsewhere
        }
        return this.consumeKeyString();
    }

    protected consumeStringFromSource(source: string, startPosition: number, current: number): string {
        let currentPosition = current;
        let lastPosition = startPosition;
        let char = source[currentPosition];
        let usedAppend = false;

        while (char !== STRING) { // Assuming STRING is defined elsewhere
            if (char === STRING_ESC) { // Assuming STRING_ESC is defined elsewhere
                usedAppend = true;
                currentPosition = this.prefetchOrEof(this.appendEscape(lastPosition, currentPosition));
                if (currentPosition === -1) {
                    this.failString("Unexpected EOF", currentPosition);
                }
                lastPosition = currentPosition;
            } else if (++currentPosition >= source.length) {
                usedAppend = true;
                this.appendRange(lastPosition, currentPosition);
                currentPosition = this.prefetchOrEof(currentPosition);
                if (currentPosition === -1) {
                    this.failString("Unexpected EOF", currentPosition);
                }
                lastPosition = currentPosition;
            }
            char = source[currentPosition];
        }

        const string = !usedAppend ? this.substring(lastPosition, currentPosition)
            : this.decodedString(lastPosition, currentPosition);
        this.currentPosition = currentPosition + 1;
        return string;
    }

    private appendEscape(lastPosition: number, current: number): number {
        this.appendRange(lastPosition, current);
        return this.appendEsc(current + 1); // Assuming appendEsc is defined elsewhere
    }

    private decodedString(lastPosition: number, currentPosition: number): string {
        this.appendRange(lastPosition, currentPosition);
        const result = this.escapedString;
        this.escapedString = '';
        return result;
    }

    private takePeeked(): string {
        const result = this.peekedString || ''; // Handling non-null assertion
        this.peekedString = null;
        return result;
    }

    consumeStringLenientNotNull(): string {
        const result = this.consumeStringLenient(); // Assuming consumeStringLenient is defined elsewhere
        if (result === NULL && this.wasUnquotedString()) { // Assuming NULL is defined elsewhere
            this.failString("Unexpected 'null' value instead of string literal");
        }
        return result;
    }

    private wasUnquotedString(): boolean {
        return this.source[this.currentPosition - 1] !== STRING; // Assuming STRING is defined elsewhere
    }

    consumeStringLenient(): string {
        if (this.peekedString !== null) {
            return this.takePeeked();
        }
        let current = this.skipWhitespaces();
        if (current >= this.source.length || current === -1) this.failString("EOF", current);
        const token = charToTokenClass(this.source[current]); // Assuming charToTokenClass is defined elsewhere
        if (token === TC_STRING) { // Assuming TC_STRING is defined elsewhere
            return this.consumeString(); // Assuming consumeString is defined elsewhere
        }

        if (token !== TC_OTHER) { // Assuming TC_OTHER is defined elsewhere
            this.failString(`Expected beginning of the string, but got ${this.source[current]}`);
        }
        let usedAppend = false;
        while (charToTokenClass(this.source[current]) === TC_OTHER) {
            ++current;
            if (current >= this.source.length) {
                usedAppend = true;
                this.appendRange(this.currentPosition, current);
                const eof = this.prefetchOrEof(current); // Assuming prefetchOrEof is defined elsewhere
                if (eof === -1) {
                    this.currentPosition = current;
                    return this.decodedString(0, 0); // Assuming decodedString is defined elsewhere
                } else {
                    current = eof;
                }
            }
        }
        const result = !usedAppend ? this.substring(this.currentPosition, current)
            : this.decodedString(this.currentPosition, current);
        this.currentPosition = current;
        return result;
    }

    protected appendRange(fromIndex: number, toIndex: number): void {
        this.escapedString += this.source.substring(fromIndex, toIndex);
    }

    private appendEsc(startPosition: number): number {
        let currentPosition = startPosition;
        currentPosition = this.prefetchOrEof(currentPosition);
        if (currentPosition === -1) this.failString("Expected escape sequence to continue, got EOF");
        const currentChar = this.source[currentPosition++];
        if (currentChar === UNICODE_ESC) { // Assuming UNICODE_ESC is defined elsewhere
            return this.appendHex(this.source, currentPosition); // Assuming appendHex is defined elsewhere
        }

        const c = escapeToChar(currentChar.charCodeAt(0)); // Assuming escapeToChar is defined elsewhere
        if (c === INVALID) this.failString(`Invalid escaped char '${currentChar}'`); // Assuming INVALID is defined elsewhere
        this.escapedString += c;
        return currentPosition;
    }

    private appendHex(source: string, startPos: number): number {
        if (startPos + 4 >= source.length) {
            this.currentPosition = startPos;
            this.ensureHaveChars(); // Assuming ensureHaveChars is defined elsewhere
            if (this.currentPosition + 4 >= source.length)
                this.failString("Unexpected EOF during unicode escape");
            return this.appendHex(source, this.currentPosition);
        }
        this.escapedString += String.fromCharCode(
            (this.fromHexChar(source, startPos) << 12) + // Assuming fromHexChar is defined elsewhere
            (this.fromHexChar(source, startPos + 1) << 8) +
            (this.fromHexChar(source, startPos + 2) << 4) +
            this.fromHexChar(source, startPos + 3)
        );
        return startPos + 4;
    }

    require(condition: boolean, position: number = this.currentPosition, message: () => string): void {
        if (!condition) this.failString(message(), position);
    }

    requireCurrentPos(condition: boolean, message: () => string): void {
        this.require(condition, this.currentPosition, message)
    }


    private fromHexChar(source: string, currentPosition: number): number {
        const character = source[currentPosition];
        if (character >= '0' && character <= '9') {
            return character.charCodeAt(0) - '0'.charCodeAt(0);
        } else if (character >= 'a' && character <= 'f') {
            return character.charCodeAt(0) - 'a'.charCodeAt(0) + 10;
        } else if (character >= 'A' && character <= 'F') {
            return character.charCodeAt(0) - 'A'.charCodeAt(0) + 10;
        } else {
            this.failString(`Invalid toHexChar char '${character}' in unicode escape`);
        }
    }

    skipElement(allowLenientStrings: boolean): void {
        const tokenStack: number[] = []; // Byte in Kotlin, number in TypeScript
        let lastToken = this.peekNextToken(); // Assuming peekNextToken is defined elsewhere
        if (lastToken !== TC_BEGIN_LIST && lastToken !== TC_BEGIN_OBJ) { // Assuming TC_BEGIN_LIST and TC_BEGIN_OBJ are defined elsewhere
            this.consumeStringLenient(); // Assuming consumeStringLenient is defined elsewhere
            return;
        }
        while (true) {
            lastToken = this.peekNextToken();
            if (lastToken === TC_STRING) { // Assuming TC_STRING is defined elsewhere
                if (allowLenientStrings) this.consumeStringLenient(); else this.consumeKeyString(); // Assuming consumeKeyString is defined elsewhere
                continue;
            }
            switch (lastToken) {
                case TC_BEGIN_LIST:
                case TC_BEGIN_OBJ:
                    tokenStack.push(lastToken);
                    break;
                case TC_END_LIST: // Assuming TC_END_LIST is defined elsewhere
                    if (tokenStack[tokenStack.length - 1] !== TC_BEGIN_LIST) { // Assuming TC_BEGIN_LIST is defined elsewhere
                        throw new JsonDecodingException(
                            `found ] instead of } at path: ${this.path.getPath()}`, // Assuming path and getPath are defined elsewhere
                        );
                    }
                    tokenStack.pop();
                    break;
                case TC_END_OBJ: // Assuming TC_END_OBJ is defined elsewhere
                    if (tokenStack[tokenStack.length - 1] !== TC_BEGIN_OBJ) { // Assuming TC_BEGIN_OBJ is defined elsewhere
                        throw new JsonDecodingException(
                            `found } instead of ] at path: ${this.path.getPath()}`,
                        );
                    }
                    tokenStack.pop();
                    break;
                case TC_EOF: // Assuming TC_EOF is defined elsewhere
                    this.failString("Unexpected end of input due to malformed JSON during ignoring unknown keys");
            }
            this.consumeNextToken(); // Assuming consumeNextToken is defined elsewhere
            if (tokenStack.length === 0) return;
        }
    }

    toString(): string {
        return `JsonReader(source='${this.source}', currentPosition=${this.currentPosition})`;
    }

    failOnUnknownKey(key: string): void {
        const processed = this.source.substring(0, this.currentPosition);
        const lastIndexOf = processed.lastIndexOf(key);
        this.failString(`Encountered an unknown key '${key}'`, lastIndexOf, ignoreUnknownKeysHint); // Assuming ignoreUnknownKeysHint is defined elsewhere
    }

    failString(message: string, position: number = this.currentPosition, hint: string = ""): never {
        const hintMessage = hint ? `\n${hint}` : "";
        throw createJsonDecodingExceptionWithInput(position, message + " at path: " + this.path.getPath() + hintMessage, this.source); // Assuming JsonDecodingException is defined elsewhere
    }

    consumeNumericLiteral(): number {
        let current = this.skipWhitespaces(); // Assuming skipWhitespaces is defined elsewhere
        current = this.prefetchOrEof(current); // Assuming prefetchOrEof is defined elsewhere
        if (current >= this.source.length || current === -1) this.failString("EOF");

        let hasQuotation = this.source[current] === STRING; // Assuming STRING is defined elsewhere
        if (hasQuotation) {
            if (++current === this.source.length) this.failString("EOF");
        }

        let accumulator = 0;
        let exponentAccumulator = 0;
        let isNegative = false;
        let isExponentPositive = false;
        let hasExponent = false;
        const start = current;

        while (current !== this.source.length) {
            const ch = this.source[current];
            if ((ch === 'e' || ch === 'E') && !hasExponent) {
                if (current === start) this.failString(`Unexpected symbol ${ch} in numeric literal`);
                isExponentPositive = true;
                hasExponent = true;
                ++current;
                continue;
            }

            // Handling negative sign and plus sign in exponent
            if ((ch === '-' || ch === '+') && hasExponent) {
                if (current === start) this.failString(`Unexpected symbol ${ch} in numeric literal`);
                isExponentPositive = ch === '+';
                ++current;
                continue;
            }

            // Handling negative sign at start
            if (ch === '-' && current === start) {
                isNegative = true;
                ++current;
                continue;
            }

            const token = charToTokenClass(ch); // Assuming charToTokenClass is defined elsewhere
            if (token !== TC_OTHER) break; // Assuming TC_OTHER is defined elsewhere

            ++current;
            const digit = ch.charCodeAt(0) - '0'.charCodeAt(0);
            if (digit < 0 || digit > 9) this.failString(`Unexpected symbol '${ch}' in numeric literal`);

            if (hasExponent) {
                exponentAccumulator = exponentAccumulator * 10 + digit;
                continue;
            }

            accumulator = accumulator * 10 - digit;
            if (accumulator > 0) this.failString("Numeric value overflow");
        }

        const hasChars = current !== start;
        if (!hasChars || (isNegative && start === current - 1)) {
            this.failString("Expected numeric literal");
        }

        if (hasQuotation) {
            if (!hasChars) this.failString("EOF");
            if (this.source[current] !== STRING) this.failString("Expected closing quotation mark");
            ++current;
        }

        this.currentPosition = current;

        if (hasExponent) {
            const doubleAccumulator = accumulator * Math.pow(10, isExponentPositive ? exponentAccumulator : -exponentAccumulator);
            if (doubleAccumulator > Number.MAX_SAFE_INTEGER || doubleAccumulator < Number.MIN_SAFE_INTEGER) this.failString("Numeric value overflow");
            accumulator = Math.floor(doubleAccumulator);
        }

        return isNegative ? accumulator : -accumulator;
    }

    consumeBoolean(): boolean {
        return this.consumeBooleanFromPosition(this.skipWhitespaces()); // Assuming skipWhitespaces is defined elsewhere
    }

    consumeBooleanLenient(): boolean {
        let current = this.skipWhitespaces();
        if (current === this.source.length) this.failString("EOF");

        let hasQuotation = this.source[current] === STRING; // Assuming STRING is defined elsewhere
        if (hasQuotation) {
            ++current;
        }

        const result = this.consumeBooleanFromPosition(current);

        if (hasQuotation) {
            if (this.currentPosition === this.source.length) this.failString("EOF");
            if (this.source[this.currentPosition] !== STRING)
                this.failString("Expected closing quotation mark");
            ++this.currentPosition;
        }

        return result;
    }

    private consumeBooleanFromPosition(start: number): boolean {
        let current = this.prefetchOrEof(start); // Assuming prefetchOrEof is defined elsewhere
        if (current >= this.source.length || current === -1) this.failString("EOF");

        const ch = this.source[current++].charCodeAt(0) | asciiCaseMask;
        switch (ch) {
            case 't'.charCodeAt(0):
                this.consumeBooleanLiteral("rue", current);
                return true;
            case 'f'.charCodeAt(0):
                this.consumeBooleanLiteral("alse", current);
                return false;
            default:
                this.failString(`Expected valid boolean literal prefix, but had '${this.consumeStringLenient()}'`); // Assuming consumeStringLenient is defined elsewhere
        }
    }

    private consumeBooleanLiteral(literalSuffix: string, current: number): void {
        if (this.source.length - current < literalSuffix.length) {
            this.failString("Unexpected end of boolean literal");
        }

        for (let i = 0; i < literalSuffix.length; i++) {
            const expected = literalSuffix.charCodeAt(i);
            const actual = this.source.charCodeAt(current + i) | asciiCaseMask;
            if (expected !== actual) {
                this.failString(`Expected valid boolean literal prefix, but had '${this.consumeStringLenient()}'`);
            }
        }

        this.currentPosition = current + literalSuffix.length;
    }

    withPositionRollback<T>(action: () => T): T {
        const snapshot = this.currentPosition;
        try {
            return action();
        } finally {
            this.currentPosition = snapshot;
        }
    }

}

