/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {SerialDescriptor} from "../core/SerialDescriptor";
import {DECODER_DECODE_DONE} from "../core/encoding/Decoding";

export class ElementMarker {
    private descriptor: SerialDescriptor;
    private readIfAbsent: (descriptor: SerialDescriptor, index: number) => boolean;
    private lowerMarks: number;
    private highMarksArray: number[];
    private static EMPTY_HIGH_MARKS: number[] = [];

    constructor(descriptor: SerialDescriptor, readIfAbsent: (descriptor: SerialDescriptor, index: number) => boolean) {
        this.descriptor = descriptor;
        this.readIfAbsent = readIfAbsent;

        const elementsCount = descriptor.elementsCount;
        if (elementsCount <= 64) {
            this.lowerMarks = elementsCount === 64 ? 0 : -1 << elementsCount;
            this.highMarksArray = ElementMarker.EMPTY_HIGH_MARKS;
        } else {
            this.lowerMarks = 0;
            this.highMarksArray = this.prepareHighMarksArray(elementsCount);
        }
    }

    mark(index: number): void {
        if (index < 64) {
            this.lowerMarks |= 1 << index;
        } else {
            this.markHigh(index);
        }
    }

    nextUnmarkedIndex(): number {
        const elementsCount = this.descriptor.elementsCount;
        while (this.lowerMarks !== -1) {
            const index = ~this.lowerMarks & (this.lowerMarks + 1);
            this.lowerMarks |= 1 << index;

            if (this.readIfAbsent(this.descriptor, index)) {
                return index;
            }
        }

        if (elementsCount > 64) {
            return this.nextUnmarkedHighIndex();
        }
        return DECODER_DECODE_DONE;
    }

    private prepareHighMarksArray(elementsCount: number): number[] {
        const slotsCount = (elementsCount - 1) >> 6;
        const elementsInLastSlot = elementsCount & 63;
        const highMarks = new Array(slotsCount).fill(0);

        if (elementsInLastSlot !== 0) {
            highMarks[highMarks.length - 1] = -1 << elementsCount;
        }
        return highMarks;
    }

    private markHigh(index: number): void {
        const slot = (index >> 6) - 1;
        const offsetInSlot = index & 63;
        this.highMarksArray[slot] |= 1 << offsetInSlot;
    }

    private nextUnmarkedHighIndex(): number {
        for (let slot = 0; slot < this.highMarksArray.length; slot++) {
            const slotOffset = (slot + 1) * 64;
            let slotMarks = this.highMarksArray[slot];

            while (slotMarks !== -1) {
                const indexInSlot = ~slotMarks & (slotMarks + 1);
                slotMarks |= 1 << indexInSlot;

                const index = slotOffset + indexInSlot;
                if (this.readIfAbsent(this.descriptor, index)) {
                    this.highMarksArray[slot] = slotMarks;
                    return index;
                }
            }
            this.highMarksArray[slot] = slotMarks;
        }
        return DECODER_DECODE_DONE;
    }
}