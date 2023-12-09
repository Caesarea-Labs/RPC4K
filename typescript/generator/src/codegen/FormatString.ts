import {Dayjs} from "dayjs";
import {recordToArray} from "rpc4ts-runtime/src/impl/Util";

/**
 * Strings may be formatted with %T to replace %T with some reference that will be automatically imported.
 */
export class FormatString {
    str: string
    formatArguments: TsReference[]

    constructor(str: string, formatArguments: TsReference[]) {
        this.str = str
        this.formatArguments = formatArguments
    }

    concat(other: MaybeFormattedString): FormatString {
        const otherFormat = toFormat(other)
        return new FormatString(this.str + otherFormat.str, this.formatArguments.concat(otherFormat.formatArguments))
        // if (typeof other === "string") {
        //     return new FormatString(this.str + other, this.formatArguments)
        // } else if (isTypescriptReference(other)) {
        //     return new FormatString(this.str + "%T", this.formatArguments.concat(other))
        // } else {
        //     return new FormatString(this.str + other.str, this.formatArguments.concat(other.formatArguments))
        // }
    }

    resolve(): string {
        //TODO
        throw new Error("TODO")
    }
}

export type MaybeFormattedString = FormatString | string | TsReference
export function resolveMaybeFormatString(str : MaybeFormattedString): string {
    if(isTypescriptReference(str)) {
        return tsReferenceToString(str)
    } else if(typeof str === "string") return str
    else {
        return str.resolve()
    }
}

function isTypescriptReference(obj: MaybeFormattedString): obj is TsReference {
    return typeof obj !== "string" && !("formatArguments" in obj)
}


export function format(str: string, ...formatArguments: TsReference[]): FormatString {
    return new FormatString(str, formatArguments)
}

export function concat(str: MaybeFormattedString, ...others: MaybeFormattedString[]): MaybeFormattedString {
    let current = str
    for (const other of others) {
        current = concat2(current, other)
    }
    return current
}

export function join(strings: MaybeFormattedString[], delimiter: string): MaybeFormattedString {
    if (strings.length === 0) return ""
    let current = strings[0]
    if (strings.length === 1) return current
    for (let i = 1; i < strings.length; i++) {
        current = concat(current, delimiter, strings[i])
    }
    return current
}

function concat2(str: MaybeFormattedString, formatString: MaybeFormattedString): FormatString {
    return toFormat(str).concat(formatString)
}

function toFormat(str: MaybeFormattedString): FormatString {
    if (typeof str === "string") {
        return new FormatString(str, [])
    } else if (isTypescriptReference(str)) {
        return new FormatString("%R", [str])
    } else {
        return str
    }

    // return typeof str === "string" ? new FormatString(str, []) : str
}

//TODo: adding this for now for compatibility, in the future i don't want to do the newline calculation shenanigans
// and use a better approach
export function formatLength(str: MaybeFormattedString): number {
    return toFormat(str).str.length
}

export type TsReference = TsType | TsFunction

export class TsNamespace {
    name: string
    importPath: ImportPath

    constructor(name: string, importPath: ImportPath) {
        this.name = name
        this.importPath = importPath
    }

    static library(name: string, importPath: string): TsNamespace {
        return new TsNamespace(name, {value: importPath, libraryPath: true})
    }

    function(name: string): TsFunction {
        return TsFunction.namespaced(this, name)
    }
}

// export namespace TsNamespaces {
//     export function library(name: string, importPath: string): Namespace {
//         return {
//             name,
//             importPath: {
//                 value: importPath,
//                 libraryPath: true
//             }
//         }
//     }
// }


export class TsFunction {
    name: string
    namespace: TsNamespace | undefined
    importPath: ImportPath

    static namespaced(namespace: TsNamespace, name: string): TsFunction {
        return new TsFunction(name, namespace.importPath, namespace)
    }

    static user(name: string, importPath: string): TsFunction {
        return new TsFunction(name, {
            value: importPath,
            libraryPath: false
        }, undefined)
    }

    // static library(name: string,importPath: string, {namespace}: { namespace: string }): TsFunction {
    //     return new TsFunction(name, {
    //         value: importPath,
    //         libraryPath: true
    //     }, namespace)
    // }

    constructor(name: string, importPath: ImportPath, namespace?: TsNamespace) {
        this.name = name
        this.importPath = importPath
        this.namespace = namespace
    }

    private readonly _brand: void = undefined
}

// export function isTsFunction(reference: TsReference): reference is TsFunction {
//
// }


export type TsType = TsUnionType | TsBasicType | TsObjectType

export function tsReferenceToString(reference: TsReference): string {
    if (reference instanceof TsFunction) {
        return `${reference.name}()`
    } else if (isUnionType(reference)) {
        return reference.references.map(reference => tsReferenceToString(reference)).join(" | ")
    } else if (isBasicType(reference)) {
        return reference.name
    } else {
        return "{" + recordToArray(reference.properties, (k, v) => `${k}: ${tsReferenceToString(v)}`)
            .join(", ") + "}"
    }
}

export interface TsUnionType {
    references: TsReference[]
}

export interface TsObjectType {
    properties: Record<string, TsType>
}

export function isUnionType(reference: TsReference): reference is TsUnionType {
    return "references" in type
}

export interface ImportPath {
    libraryPath: boolean
    value: string
}

export interface TsBasicType {
    name: string
    typeArguments: TsReference[]
    importPath: ImportPath | undefined

    // constructor(name: string, importPath?: string, typeArguments?: TypescriptReference[]) {
    //     this.name = name
    //     this.importPath = importPath
    //     this.typeArguments = typeArguments ?? []
    // }
}

export function isBasicType(reference: TsReference): reference is TsBasicType {
    return "typeArguments" in reference
}

function type(name: string, importPath: string, ...typeArguments: TsReference[]): TsBasicType {
    return {
        name, importPath: {libraryPath: false, value: importPath},
        typeArguments
    }
}

function builtin(name: string, ...typeArguments: TsReference[]): TsBasicType {
    return {
        importPath: undefined,
        name,
        typeArguments: typeArguments,
    }
}

export const TupleTypeName = "_tuple"

export namespace TsTypes {
    export const STRING = builtin("string")
    export const NULL = builtin("null")
    export const NUMBER = builtin("number")
    export const BOOLEAN = builtin("boolean")
    export const VOID = builtin("void")

    export function array(elementType: TsType): TsBasicType {
        //TODO: add special handling to treat "Array" as "[]"
        return builtin("Array", elementType)
    }

    export function stringLiteral(value: string): TsBasicType {
        return builtin(`"${value}"`)
    }

    export function record(keyType: TsType, valueType: TsType): TsBasicType {
        return builtin("Record", keyType, valueType)
    }

    export function promise(type: TsType): TsBasicType {
        return builtin("Promise", type)
    }

    export function tuple(types: TsType[]): TsBasicType {
        //TODO: add special handling to treat "_tuple" as "[]"
        return builtin(TupleTypeName, ...types)
    }

    export const DURATION = type("Duration", "dayjs/plugin/duration")
    export const DAYJS = type("Dayjs", "dayjs")

    export function nullable(type: TsType): TsUnionType {
        return {
            references: [type, NULL]
        }
    }

    export function typeParameter(name: string): TsBasicType {
        return {
            importPath: undefined,
            name,
            typeArguments: [],
        }
    }

    export function create(name: string, importPath: string, ...typeArguments: TsType[]): TsBasicType {
        return {
            importPath: {
                value: importPath,
                libraryPath: false
            },
            name, typeArguments
        }
    }

    export function library(name: string, importPath: string, ...typeArguments: TsType[]): TsBasicType {
        return {
            importPath: {
                value: importPath, libraryPath: false
            },
            name, typeArguments
        }
    }
}

// export type ReferenceString = FormatString | string