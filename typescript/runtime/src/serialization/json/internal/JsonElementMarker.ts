/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {ElementMarker} from "../../internal/ElementMarker";
import {SerialDescriptor} from "../../core/SerialDescriptor";

export class JsonElementMarker {
    private origin: ElementMarker;
    private _isUnmarkedNull: boolean = false;

    constructor(descriptor: SerialDescriptor) {
        this.origin = new ElementMarker(descriptor, this.readIfAbsent.bind(this));
    }

    public get isUnmarkedNull(): boolean {
        return this._isUnmarkedNull;
    }

    public mark(index: number): void {
        this.origin.mark(index);
    }

    public nextUnmarkedIndex(): number {
        return this.origin.nextUnmarkedIndex();
    }

    private readIfAbsent(descriptor: SerialDescriptor, index: number): boolean {
        this._isUnmarkedNull = !descriptor.isElementOptional(index) && descriptor.getElementDescriptor(index).isNullable;
        return this._isUnmarkedNull;
    }
}

