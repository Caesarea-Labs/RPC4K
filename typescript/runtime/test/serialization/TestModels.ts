import {TsSerializer} from "../../src/serialization/TsSerializer";
import {ArraySerializer, BooleanSerializer, NumberSerializer, StringSerializer} from "../../src/serialization/BuiltinSerializers";
import {GeneratedSerializerImpl} from "../../src/serialization/GeneratedSerializer";
import {EnumSerializer} from "../../src/serialization/EnumSerializer";
import {UnionSerializer} from "../../src/serialization/UnionSerializer";

export type TestEnum = "a" | "b" | "c"

export type TestUnion<T> = AnotherModelHolder<string> | GenericThing<T, number>

export class AnotherModelHolder<T> {
    readonly t: GenericThing<T, string>

    constructor({t}: { t: GenericThing<T, string> }) {
        this.t = t;
    }

    private _rpc_name = "AnotherModelHolder"
}

export namespace Rpc4tsSerializers {
    export function testUnion<T>(argSerializer: TsSerializer<T>): TsSerializer<TestUnion<T>> {
        return new UnionSerializer<TestUnion<T>>("TestUnion",  "TestUnion",
            ["AnotherModelHolder",  "GenericThing"],
            [anotherModelHolder(StringSerializer), genericThing(argSerializer, NumberSerializer)]
        )
    }

    export function testEnum(): TsSerializer<TestEnum> {
        return new EnumSerializer("TestEnum", ["a", "b", "c"])
    }

    export function anotherModelHolder<T>(typeArgument1: TsSerializer<T>): TsSerializer<AnotherModelHolder<T>> {
        return new GeneratedSerializerImpl<AnotherModelHolder<T>>(
            "AnotherModelHolder",
            {t: genericThing(typeArgument1, StringSerializer)},
            [typeArgument1],
            (params) => new AnotherModelHolder(params)
        )
    }

    export function genericThing<T1, T2>(typeArgument1: TsSerializer<T1>, typeArgument2: TsSerializer<T2>): TsSerializer<GenericThing<T1, T2>> {
        return new GeneratedSerializerImpl<GenericThing<T1, T2>>(
            "GenericThing",
            {x: typeArgument1, w: new ArraySerializer(typeArgument2), a: StringSerializer},
            [typeArgument1],
            (params) => new GenericThing(params)
        )
    }

    export function foo(): TsSerializer<Foo> {
        return new GeneratedSerializerImpl<Foo>(
            "Foo",
            {x: NumberSerializer, y: StringSerializer, z: BooleanSerializer},
            [],
            (params) => new Foo(params)
        )
    }

    export function rpc4ts_serializer_Tree<T0>(typeArg0: TsSerializer<T0>): TsSerializer<Tree<T0>> {
        return new GeneratedSerializerImpl<Tree<T0>>(
            "Tree",
            {
                item: typeArg0,
                children: () => new ArraySerializer(rpc4ts_serializer_Tree(typeArg0))
            },
            [typeArg0],
            params => params
        )
    }
}

export interface Tree<T> {
    item: T,
    children: Tree<T>[]
}

export class GenericThing<T1, T2> {
    readonly x: T1
    readonly w: T2[]
    readonly a: string

    constructor({x, w, a}: { x: T1, w: T2[], a: string }) {
        this.x = x
        this.w = w
        this.a = a
    }
    private _rpc_name = "AnotherModelHolder"
}


export class Foo {
    readonly x: number
    readonly y: string
    readonly z: boolean

    constructor({x, y, z}: { x: number, y: string, z: boolean }) {
        this.x = x
        this.y = y
        this.z = z
    }
}