
export interface JsonWriter {
    writeNumber(value: number): void;
    write(text: string): void;
    writeQuoted(text: string): void;
}