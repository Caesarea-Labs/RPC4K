import {recordToArray} from "ts-minimum"

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

    /**
     * Inserts the format arguments in place of the reference markers
     */
    resolve(): string {
        let retval = ""
        let formatIndex = 0
        let prevChar: string | null = null
        for (const char of this.str) {
            if (prevChar === FORMAT_MARKER && char === REFERENCE_MARKER) {
                if (this.formatArguments.length > formatIndex) {
                    retval += tsReferenceToString(this.formatArguments[formatIndex])
                    formatIndex++
                    // Don't include the REFERENCE_MARKER in the string
                    prevChar = null
                } else {
                    throw new Error(`No format argument specified for ${formatIndex}th format marker`)
                }
            } else {
                if (prevChar !== null) {
                    retval += prevChar
                }
                prevChar = char
            }
        }
        if (prevChar !== null) retval += prevChar
        return retval
    }
}

export type MaybeFormattedString = FormatString | string | TsReference

// export function isFormatString(maybeFormatString: MaybeFormattedString): maybeFormatString is FormatString {
//     return maybeFormatString instanceof FormatString
// }

export function resolveMaybeFormatString(str: MaybeFormattedString): string {
    if (isTypescriptReference(str)) {
        return tsReferenceToString(str)
    } else if (typeof str === "string") return str
    else {
        return str.resolve()
    }
}

export function isTypescriptReference(obj: MaybeFormattedString): obj is TsReference {
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

const REFERENCE_MARKER = "R"
const FORMAT_MARKER = "%"

function toFormat(str: MaybeFormattedString): FormatString {
    if (typeof str === "string") {
        return new FormatString(str, [])
    } else if (isTypescriptReference(str)) {
        return new FormatString(FORMAT_MARKER + REFERENCE_MARKER, [str])
    } else {
        return str
    }

    // return typeof str === "string" ? new FormatString(str, []) : str
}

//NiceToHave: Improve wrapping of generated Typescript file
// adding this for now for compatibility, in the future i don't want to do the newline calculation shenanigans
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
//
// /**
//  * Will resolve the type on the spot to prevent it from being imported
//  */
// export function dontImport(type: TsType): string {
//     return resolveMaybeFormatString(type)
// }

/**
 * Some type literals like union types are sensitive to brackets around the type, so if this type is being wrapped
 * by another type like an array then brackets are required.
 */
export function tsReferenceToString(reference: TsReference, brackets = false): string {
    if (reference instanceof TsFunction) {
        const namespace = reference.namespace !== undefined ? `${reference.namespace.name}.` : ""
        return namespace + reference.name
    } else if (isUnionType(reference)) {
        const literal = reference.references.map(reference => tsReferenceToString(reference)).join(" | ")
        return brackets ? `(${literal})` : literal
    } else if (isBasicType(reference)) {
        if (reference.name === TupleTypeName) {
            // Represent a tuple as [  , ... ]
            return "[" + reference.typeArguments.map(arg => tsReferenceToString(arg)).join(", ") + "]"
        }
        if (reference.name === ArrayTypeName) {
            const elementType = reference.typeArguments[0]
            const elementString = tsReferenceToString(elementType)
            const needsBrackets = isUnionType(elementType)
            const finalElementString = needsBrackets ? `(${elementString})` : elementString
            return `${finalElementString}[]`
        }
        let str = reference.name
        if (reference.typeArguments.length > 0) {
            str += `<${reference.typeArguments.map(arg => tsReferenceToString(arg)).join(", ")}>`
        }
        return str
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
    return "references" in reference
}

export interface ImportPath {
    libraryPath: boolean
    value: string
}

export interface TsBasicType {
    name: string
    typeArguments: TsType[]
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

function type(name: string, importPath: string, ...typeArguments: TsType[]): TsBasicType {
    return {
        name, importPath: {libraryPath: false, value: importPath},
        typeArguments
    }
}

function builtin(name: string, ...typeArguments: TsType[]): TsBasicType {
    return {
        importPath: undefined,
        name,
        typeArguments: typeArguments,
    }
}

export const TupleTypeName = "_tuple"
export const ArrayTypeName = "Array"
export namespace TsTypes {
    export const STRING = builtin("string")
    export const NULL = builtin("null")
    export const NUMBER = builtin("number")
    export const BOOLEAN = builtin("boolean")
    export const VOID = builtin("void")
    export const i8Array = builtin("Int8Array")
    export const ui8Array = builtin("Uint8Array")

    export function array(elementType: TsType): TsBasicType {
        return builtin(ArrayTypeName, elementType)
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
                value: importPath, libraryPath: true
            },
            name, typeArguments
        }
    }
}

// export type ReferenceString = FormatString | string