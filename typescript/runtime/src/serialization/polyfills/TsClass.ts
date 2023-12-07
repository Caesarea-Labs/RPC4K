//TODO: we can lose the <T> i think
export type TsClass<T>  = string
    // name: string
// }

export function getTsClass(value: unknown): TsClass<unknown> {
    // We inject _rpc_name fields in classes that we may want to get the type name of in runtime.
    //@ts-ignore
    return value["_rpc_name"]

        // name:
    // }
}