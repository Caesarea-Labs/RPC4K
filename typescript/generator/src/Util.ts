import {buildRecord} from "rpc4ts-runtime/src/impl/Util";

export function uniqueBy<T>(arr: T[], value: (element: T) => string): T[] {
    const record = buildRecord(arr, (element) => [value(element), element])
    return Object.values(record)
}