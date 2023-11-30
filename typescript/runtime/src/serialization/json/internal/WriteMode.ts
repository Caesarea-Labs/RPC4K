/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {BEGIN_LIST, BEGIN_OBJ, END_LIST, END_OBJ} from "./AbstractJsonLexer";
import {Json} from "../Json";
import {SerialDescriptor} from "../../core/SerialDescriptor";
import {isPrimitiveKind, OtherSerialType, PolymorphicKind, StructureKind} from "../../core/SerialKind";
import {InvalidKeyKindException} from "./JsonExceptions";



export interface WriteMode {
    value: string
    begin: string
    end: string
    ordinal: number
}

export namespace WriteModes {
    export const OBJ = {value: "OBJ", begin: BEGIN_OBJ, end: END_OBJ, ordinal: 0}
    export const LIST =  {value: "LIST", begin: BEGIN_LIST, end: END_LIST, ordinal: 1}
    export const MAP ={value: "MAP", begin: BEGIN_OBJ, end: END_OBJ, ordinal: 2}
    export const POLY_OBJ = {value: "POLY_OBJ", begin: BEGIN_LIST, end: END_LIST, ordinal: 3}
}

export const WriteModeValues: WriteMode[] = [WriteModes.OBJ, WriteModes.LIST, WriteModes.MAP, WriteModes.POLY_OBJ]

// export enum WriteMode {
//     OBJ = "OBJ",
//     LIST = "LIST",
//     MAP = "MAP",
//     POLY_OBJ = "POLY_OBJ",
// }
//
// export class WriteModeProperties {
//     public begin: string;
//     public end: string;
//     public ordinal: number
//
//     constructor(begin: string, end: string, ordinal: number) {
//         this.begin = begin;
//         this.end = end;
//         this.ordinal = ordinal
//     }
//
//     static properties = {
//         [WriteMode.OBJ]: new WriteModeProperties(BEGIN_OBJ, END_OBJ, 0),
//         [WriteMode.LIST]: new WriteModeProperties(BEGIN_LIST, END_LIST, 1),
//         [WriteMode.MAP]: new WriteModeProperties(BEGIN_OBJ, END_OBJ, 2),
//         [WriteMode.POLY_OBJ]: new WriteModeProperties(BEGIN_LIST, END_LIST, 3),
//     };
//
//     static getProperties(mode: WriteMode): WriteModeProperties {
//         return this.properties[mode];
//     }
// }

export function switchMode(json: Json, desc: SerialDescriptor): WriteMode {
    switch (desc.kind) {
        case PolymorphicKind.OPEN: // Assuming PolymorphicKind is an enum or similar construct
            return WriteModes.POLY_OBJ;
        case StructureKind.LIST:
            return WriteModes.LIST;
        case StructureKind.MAP:
            return selectMapMode(json, desc, () => WriteModes.MAP, () => WriteModes.LIST);
        default:
            return WriteModes.OBJ;
    }
}

function selectMapMode<T, R1 extends T, R2 extends T>(
    json: Json,
    mapDescriptor: SerialDescriptor,
    ifMap: () => R1,
    ifList: () => R2
): T {
    const keyDescriptor = carrierDescriptor(mapDescriptor.getElementDescriptor(0), json.serializersModule);
    const keyKind = keyDescriptor.kind;

    if (isPrimitiveKind(keyKind) || keyKind === OtherSerialType.ENUM) {
        return ifMap();
    } else if (json.configuration.allowStructuredMapKeys) {
        return ifList();
    } else {
        throw InvalidKeyKindException(keyDescriptor);
    }
}

function carrierDescriptor(descriptor: SerialDescriptor, module: SerializersModule): SerialDescriptor {
    if (descriptor.kind === OtherSerialType.CONTEXTUAL) {
        throw new Error("Not implemented")
        // const contextualDescriptor = module.getContextualDescriptor(descriptor);
        // return contextualDescriptor ? carrierDescriptor(contextualDescriptor, module) : descriptor;
    } else if (descriptor.isInline) {
        return carrierDescriptor(descriptor.getElementDescriptor(0), module);
    } else {
        return descriptor;
    }
}