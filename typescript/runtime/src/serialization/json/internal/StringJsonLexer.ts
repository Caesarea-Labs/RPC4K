/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {AbstractJsonLexer, charToTokenClass, STRING, STRING_ESC, TC_BEGIN_OBJ, TC_COLON, TC_EOF, TC_STRING, TC_WHITESPACE} from "./AbstractJsonLexer";
import {BATCH_SIZE} from "./ReaderJsonLexer";

export class StringJsonLexer extends AbstractJsonLexer {
    protected source: string;

    constructor(source: string) {
        super();
        this.source = source;
    }

    prefetchOrEof(position: number): number {
        return position < this.source.length ? position : -1;
    }

    consumeNextToken(): number {
        while (this.currentPosition !== -1 && this.currentPosition < this.source.length) {
            const ch = this.source[this.currentPosition++];
            const tc = charToTokenClass(ch);
            if (tc === TC_WHITESPACE) continue;
            return tc;
        }
        return TC_EOF;
    }

    tryConsumeComma(): boolean {
        const current = this.skipWhitespaces();
        if (current === this.source.length || current === -1) return false;
        if (this.source[current] === ',') {
            ++this.currentPosition;
            return true;
        }
        return false;
    }

    canConsumeValue(): boolean {
        let current = this.currentPosition;
        if (current === -1) return false;
        while (current < this.source.length) {
            const c = this.source[current];
            if (c === ' ' || c === '\n' || c === '\r' || c === '\t') {
                ++current;
                continue;
            }
            this.currentPosition = current;
            return this.isValidValueStart(c);
        }
        this.currentPosition = current;
        return false;
    }

    skipWhitespaces(): number {
        let current = this.currentPosition;
        if (current === -1) return current;
        while (current < this.source.length) {
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

    consumeNextTokenWithExpected(expected: string): void {
        if (this.currentPosition === -1) this.unexpectedToken(expected);
        while (this.currentPosition < this.source.length) {
            const c = this.source[this.currentPosition++];
            if (c === ' ' || c === '\n' || c === '\r' || c === '\t') continue;
            if (c === expected) return;
            this.unexpectedToken(expected);
        }
        this.currentPosition = -1; // for correct EOF reporting
        this.unexpectedToken(expected); // EOF
    }

    consumeKeyString(): string {
        this.consumeNextTokenWithExpected(STRING); // Assuming STRING and consumeNextToken are defined elsewhere
        const current = this.currentPosition;
        const closingQuote = this.source.indexOf('"', current);
        if (closingQuote === -1) {
            this.consumeStringLenient(); // Assuming consumeStringLenient is defined elsewhere
            this.fail(TC_STRING, false); // Assuming TC_STRING is defined and fail is a method that throws an error
        }

        for (let i = current; i < closingQuote; i++) {
            if (this.source[i] === STRING_ESC) { // Assuming STRING_ESC is defined elsewhere
                return this.consumeStringFromSource(this.source, current, i); // This method needs to be implemented
            }
        }

        this.currentPosition = closingQuote + 1;
        return this.source.substring(current, closingQuote);
    }

    consumeStringChunked(isLenient: boolean, consumeChunk: (stringChunk: string) => void): void {
        const stringToChunk = isLenient ? this.consumeStringLenient() : this.consumeString(); // Assuming consumeString is defined elsewhere
        const batchSize = BATCH_SIZE; // Assuming BATCH_SIZE is defined
        for (let i = 0; i < stringToChunk.length; i += batchSize) {
            const chunk = stringToChunk.substring(i, i + batchSize);
            consumeChunk(chunk);
        }
    }

    peekLeadingMatchingValue(keyToMatch: string, isLenient: boolean): string | null {
        const positionSnapshot = this.currentPosition;
        try {
            if (this.consumeNextToken() !== TC_BEGIN_OBJ) return null; // Assuming TC_BEGIN_OBJ is defined elsewhere
            const firstKey = this.peekString(isLenient); // Assuming peekString is defined elsewhere
            if (firstKey !== keyToMatch) return null;
            this.discardPeeked(); // Assuming discardPeeked is defined elsewhere
            if (this.consumeNextToken() !== TC_COLON) return null; // Assuming TC_COLON is defined elsewhere
            return this.peekString(isLenient);
        } finally {
            this.currentPosition = positionSnapshot;
            this.discardPeeked();
        }
    }

}

