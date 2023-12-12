// noinspection PointlessBooleanExpressionJS

import {
    concat,
    formatLength,
    FormatString,
    ImportPath,
    isBasicType,
    isTypescriptReference,
    isUnionType,
    join,
    MaybeFormattedString,
    TsFunction,
    TsReference,
    tsReferenceToString,
    TsType
} from "./FormatString";
import {HashSet, recordToArray} from "ts-minimum"

const MaxLineLength = 120;
const tabWidth = 4;

/**
 * Minimalist and ultra simple code generator based on appending to a string
 */
export class CodeBuilder {
    private code = ""
    private currentIndent = 0
    /**
     * If true, references to the runtime library will use local paths instead of npm library paths
     */
    private readonly localImports: boolean

    private importsToInsert: HashSet<Import> = HashSet.create()
    // Tracks references created in this file to avoid importing stuff from this file itself
    private referencesInFile: Set<string> = new Set()

    constructor(localImports: boolean) {
        this.localImports = localImports
    }

    build() {
        if (this.currentIndent !== 0) {
            throw new Error(`${this.currentIndent} indents were not unindented`)
        }
        return this.buildImports() + "\n" + this.code
    }

    private buildImports(): string {
        const importsByPath: Record<string, string[]> = {}

        this.importsToInsert.forEach(({reference, path}) => {
            const existing = importsByPath[path]
            // Avoid importing things from this file
            if (!this.referencesInFile.has(reference)) {
                if (existing === undefined) {
                    importsByPath[path] = [reference]
                } else {
                    existing.push(reference)
                }
            }
        })

        return recordToArray(importsByPath, (path: string, references: string[]) => `import {${references.join(", ")}} from "${path}"`)
            .join("\n")
    }


    addInterface({name, typeParameters}: {
        name: string,
        typeParameters?: string[]
    }, interfaceBuilder: (builder: InterfaceBuilder) => void): CodeBuilder {
        this.referencesInFile.add(name)
        return this._addBlock(`export interface ${name}${this.typeParametersString(typeParameters)}`, () => {
            interfaceBuilder(new InterfaceBuilder(this))
        })._addLineOfCode("") // Add empty line
    }

    addClass({name, typeParameters}: {
        name: string,
        typeParameters?: string[]
    }, classBuilder: (builder: ClassBuilder) => void): CodeBuilder {
        this.referencesInFile.add(name)
        return this._addBlock(`export class ${name}${this.typeParametersString(typeParameters)}`, () => {
            classBuilder(new ClassBuilder(this))
        })._addLineOfCode("")// Add empty line
    }

    addUnionType({name, typeParameters, types}: {
        name: string,
        typeParameters?: string[],
        types: TsType[]
    }): CodeBuilder {
        this.referencesInFile.add(name)
        const prefix = `export type ${name}${this.typeParametersString(typeParameters)} = `
        const typesJoined = types.length === 0 ? "never" : join(types, " | ")
        if (this.isTooLong(formatLength(prefix) + formatLength(typesJoined))) {
            return this._addLineOfCode(concat(prefix, join(this.indentList(types), " | ")))
        } else {
            return this._addLineOfCode(concat(prefix, typesJoined))
        }
    }

    addTypeAlias({name, typeParameters, type}: {
        name: string,
        typeParameters?: string[],
        type: TsReference
    }): CodeBuilder {
        this.referencesInFile.add(name)
        const prefix = `export type ${name}${this.typeParametersString(typeParameters)} = `
        return this._addLineOfCode(concat(prefix, type))
    }

    private typeParametersString(params: string[] | undefined): string {
        if (params === undefined || params.length === 0) return ""
        return `<${params.join(", ")}>`
    }

    addConst(name: string, value: MaybeFormattedString): CodeBuilder {
        this.referencesInFile.add(name)
        return this._addLineOfCode(concat(`export const ${name} = `, value))
    }

    addTopLevelFunction(name: string,typeArguments: TsType[], parameters: [string, TsReference][], returnType: TsReference | undefined, body: (body: BodyBuilder) => void): CodeBuilder {
        this.referencesInFile.add(name)
        const typeArgs = typeArguments.length > 0 ? concat("<",join(typeArguments, ", ") , ">") : ""
        return this._addFunction(concat(`export function `, name, typeArgs), parameters, returnType, body)
    }

    ///////////////////// Internal ////////////////

    private _addImport(identifiers: string[], path: string): CodeBuilder {
        const identifiersJoined = identifiers.join(", ")
        const unwrappedLine = `import {${identifiersJoined}} from "${path}"`
        if (this.isTooLong(unwrappedLine.length)) {
            return this._addLineOfCode(`import {\n${this.indentList(identifiers).join(",\n")}\n} from "${path}"`)
        } else {
            return this._addLineOfCode(unwrappedLine)
        }
    }

    _indent(): CodeBuilder {
        this.currentIndent++
        return this
    }

    _unindent(): CodeBuilder {
        if (this.currentIndent === 0) {
            throw new Error("Unindented more indent than exists")
        }
        this.currentIndent--
        return this
    }


    _addLineOfCode(code: MaybeFormattedString, addNewline = true): CodeBuilder {
        const [codeString, references] = this.resolveFormatString(code)
        for (const reference of references) {
            this.addImportReferences(reference)
        }
        this.code += ("\t".repeat(this.currentIndent) + codeString)
        if (addNewline) this.code += "\n"
        return this
    }


    private addImportReferences(reference: TsReference) {
        for (const imp of this.resolveReferences(reference)) {
            // Consider references unique by the combination of the name + the import path.
            // This ensure we only add each reference once.
            this.importsToInsert.put(imp)
        }
    }


    private resolveReferences(reference: TsReference): Import[] {
        const imports: Import[] = []
        this.addReferences(reference, imports)
        return imports
    }

    private addReferences(reference: TsReference, addTo: Import[]) {
        if (reference instanceof TsFunction) {
            addTo.push({
                path: this.resolveImportPath(reference.importPath),
                // If it's namespaced, import the namespace, otherwise import the function itself.
                reference: reference.namespace !== undefined ? reference.namespace.name : reference.name
            })
        } else if (isUnionType(reference)) {
            for (const child of reference.references) {
                this.addReferences(child, addTo)
            }
        } else if (isBasicType(reference)) {
            // Only references that require imports are relevant here
            if (reference.importPath !== undefined) {
                addTo.push({
                    path: this.resolveImportPath(reference.importPath),
                    reference: reference.name
                })
            }
            for (const arg of reference.typeArguments) {
                this.addReferences(arg, addTo)
            }
        } else {
            for (const propertyType of Object.values(reference.properties)) {
                this.addReferences(propertyType, addTo)
            }
        }
    }

    resolveImportPath(path: ImportPath): string {
        return path.libraryPath ? this.libraryPath(path.value) : path.value
    }

    libraryPath(path: string): string {
        if (this.localImports) return `../../src/${path}`
        else return "rpc4ts-runtime"
    }

    private resolveFormatString(code: MaybeFormattedString): [string, TsReference[]] {
        if (code instanceof FormatString) {
            return [code.resolve(), code.formatArguments]
        } else if (isTypescriptReference(code)) {
            return [tsReferenceToString(code), [code]]
        } else {
            return [code, []]
        }
    }


    _addFunction(declaration: MaybeFormattedString, parameters: [string, TsReference][], returnType: TsReference | undefined, body: (body: BodyBuilder) => void): CodeBuilder {
        const returnTypeString = returnType === undefined ? "" : concat(": ", returnType)
        const parametersString = this.parameterList(
            formatLength(this.blockStart(declaration)) + formatLength(returnTypeString), parameters.map(([name, type]) => concat(`${name}: `, type))
        )
        return this._addBlock(concat(declaration, parametersString, returnTypeString), () => {
            body(new BodyBuilder(this))
        })
    }

    _addParameterListLineOfCode(prefix: MaybeFormattedString, list: MaybeFormattedString[], addNewline = true): CodeBuilder {
        return this._addLineOfCode(concat(prefix, this.parameterList(formatLength(prefix), list)), addNewline)
    }

    /**
     * Takes care of wrapping safely
     * @param otherCodeLength the amount of characters that this line contains other than the parameter list.
     * Required for knowing whether to wrap parameters.
     */
    private parameterList(otherCodeLength: number, list: MaybeFormattedString[]): MaybeFormattedString {
        const joined = concat("(", join(list, ", "), ")")
        if (this.isTooLong(otherCodeLength + formatLength(joined))) {
            // Chop argument list into separate lines if it's too long
            return concat("(\n", join(this.indentList(list), ", \n"), "\n", this.indent(")"))
        } else {
            return joined
        }
    }

    private isTooLong(codeLength: number): boolean {
        return this.getLineLength(codeLength) > MaxLineLength
    }

    private getLineLength(codeLength: number): number {
        return this.currentIndent * tabWidth + codeLength
    }

    private indentList(list: MaybeFormattedString[]): MaybeFormattedString[] {
        return list.map(s => concat("\t".repeat(this.currentIndent + 1), s))
    }

    private indent(str: string): string {
        return "\t".repeat(this.currentIndent) + str
    }


    _addBlock(blockPrefix: MaybeFormattedString, blockBuilder: () => void): CodeBuilder {
        this._addLineOfCode(this.blockStart(blockPrefix))
        this._indent()
        blockBuilder()
        this._unindent()
        return this._addLineOfCode("}")
    }

    private blockStart(prefix: MaybeFormattedString): MaybeFormattedString {
        return concat(prefix, " {")
    }
}

interface Import {
    reference: string
    path: string
}

export class InterfaceBuilder {
    protected codegen: CodeBuilder

    constructor(codegen: CodeBuilder) {
        this.codegen = codegen
    }

    addProperty({name, type, optional, initializer}: { name: string, type?: TsType, optional?: boolean, initializer?: string }): InterfaceBuilder {
        const optionalString = optional === true ? "?" : ""
        const initializerString = initializer === undefined ? "" : ` = ${initializer}`
        const typeHint: MaybeFormattedString = type !== undefined ? concat(": ", type) : ""
        this.codegen._addLineOfCode(concat(`${name}${optionalString}`, typeHint, initializerString))
        return this
    }


}

export class ClassBuilder extends InterfaceBuilder {
    constructor(codegen: CodeBuilder) {
        super(codegen)
    }

    addProperty(property: { name: string, type?: TsType, optional?: boolean, initializer?: string }): ClassBuilder {
        return super.addProperty(property) as ClassBuilder
    }

    addConstructor(parameterList: [string, TsType][], body: (builder: BodyBuilder) => void): ClassBuilder {
        return this.addFunction("constructor", parameterList, undefined, body)
    }

    addFunction(name: string, parameterList: [string, TsType][], returnType: TsType | undefined, body: (builder: BodyBuilder) => void) {
        this.codegen._addFunction(name, parameterList, returnType, body)
        return this
    }

    addNewline(): ClassBuilder {
        this.codegen._addLineOfCode("")
        return this
    }

    addComment(comment: string): ClassBuilder {
        this.codegen._addLineOfCode(`// ${comment}`)
        return this
    }

}

export class BodyBuilder {
    protected codegen: CodeBuilder

    constructor(codegen: CodeBuilder) {
        this.codegen = codegen
    }

    addAssignment(variable: string, value: string): BodyBuilder {
        this.codegen._addLineOfCode(`${variable} = ${value}`)
        return this
    }

    addEmptyLine(): BodyBuilder {
        this.codegen._addLineOfCode("")
        return this
    }

    addReturningFunctionCall(functionName: MaybeFormattedString, args: MaybeFormattedString[], addNewline = true): BodyBuilder {
        this.codegen._addParameterListLineOfCode(concat("return ", functionName), args, addNewline)
        return this
    }

    addCast(type: TsType): BodyBuilder {
        this.codegen._addLineOfCode(concat("as ", type), false)
        return this
    }

}

//     addReturningFunctionCall(functionName: string, args: string[], chaining: (builder: FunctionChainingBuilder) => void = () => {}): FunctionChainingBuilder {
//         const builder = new FunctionChainingBuilder(this.codegen)
//         chaining(builder)
//         this.codegen._addParameterListLineOfCode(`return ${functionName}`, args, builder.build())
//         return new FunctionChainingBuilder(this.codegen)
//     }
// export class FunctionChainingBuilder {
//     private chains: string[] = []
//
//     constructor(codegen: CodeBuilder) {
//     }
//
//     addCast(type: string): BodyBuilder {
//         this.chains.push(`as ${type}`)
//     }
//
//     build(): string {
//
//     }
//
// }