import {Dayjs} from "dayjs";

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

    concat(other: MaybeFormattedString | TsReference): FormatString {
        if (typeof other === "string") {
            return new FormatString(this.str + other, this.formatArguments)
        } else if (isTypescriptReference(other)) {
            return new FormatString(this.str + "%T", this.formatArguments.concat(other))
        } else {
            return new FormatString(this.str + other.str, this.formatArguments.concat(other.formatArguments))
        }
    }
}

function isTypescriptReference(obj: FormatString | TsReference): obj is TsReference {
    return "name" in obj
}


export function format(str: string, ...formatArguments: TsReference[]): FormatString {
    return new FormatString(str, formatArguments)
}

export function concat(str: MaybeFormattedString, formatString: MaybeFormattedString | TsReference): FormatString {
    return toFormat(str).concat(formatString)
}

function toFormat(str: MaybeFormattedString): FormatString {
    return typeof str === "string" ? new FormatString(str, []) : str
}

//TODo: adding this for now for compatibility, in the future i don't want to do the newline calculation shenanigans
// and use a better approach
export function length(str: MaybeFormattedString): number {
    return (typeof str === "string") ? str.length : str.str.length
}

export type TsReference = TsType | TsFunction


export class TsFunction {
    name: string
    importPath: string

    constructor(name: string, importPath: string) {
        this.name = name
        this.importPath = importPath
    }

    private readonly _brand: void = undefined
}

// export function isTsFunction(reference: TsReference): reference is TsFunction {
//
// }


export type TsType = TsUnionType | TsBasicType

export function tsReferenceToString(reference: TsReference): string {
    if (reference instanceof TsFunction) {
        return `${reference.name}()`
    } else if (isUnionType(reference)) {
        return reference.references.map(reference => tsReferenceToString(reference)).join(" | ")
    } else {
        return reference.name
    }
}

export interface TsUnionType {
    references: TsReference[]
}

export function isUnionType(reference: TsReference): reference is TsUnionType {
    return "references" in type
}

export interface TsBasicType {
    name: string
    typeArguments: TsReference[]
    importPath: string | undefined

    // constructor(name: string, importPath?: string, typeArguments?: TypescriptReference[]) {
    //     this.name = name
    //     this.importPath = importPath
    //     this.typeArguments = typeArguments ?? []
    // }
}

export function isBasicType(reference: TsReference): reference is TsUnionType {
    return "typeArguments" in reference
}

function type(name: string, importPath: string, ...typeArguments: TsReference[]): TsBasicType {
    return {
        name,
        importPath,
        typeArguments
    }
}

function builtin(name: string, ...typeArguments: TsReference[]): TsBasicType {
    return {
        importPath: undefined,
        name,
        typeArguments: typeArguments
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
    export function record(keyType: TsType, valueType: TsType): TsBasicType {
        return builtin("Record", keyType,valueType)
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
            typeArguments: []
        }
    }

    export function create(name: string, importPath: string, typeArguments: TsType[]): TsBasicType {
        return {
            importPath,
            name, typeArguments
        }
    }
}

export type MaybeFormattedString = FormatString | string
// export type ReferenceString = FormatString | string