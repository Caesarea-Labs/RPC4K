// export type PartiallyPartial<T, K extends keyof T> = {
//     // For each property P in T
//     [P in keyof T]:
//     // If P is in K, make it optional
//     P extends K ? T[P] | undefined :
//         // Otherwise, keep it as it is
//         T[P];
// };

export type PartiallyPartial<T, K extends keyof T> = Partial<Pick<T, K>> & Omit<T, K>

export function recordForEach<K extends keyof object, V>(record: Record<K, V>, iterator: (key: K, value: V, i: number) => void) {
    // @ts-ignore
    objectForEach(record, iterator)
}

export function recordMapValues<K extends keyof object, V, Res>(record: Record<K, V>, map: (key: K, value: V) => Res): Record<K, Res> {
    // @ts-ignore
    return objectMapValues(record, map)
}

export function objectForEach(obj: object, iterator: (key: string, value: unknown, index: number) => void) {
    let i = 0;
    for (const key in obj) {
        // @ts-ignore
        iterator(key, obj[key], i)
        i++
    }
}

export function objectMapValues(obj: object, map: (key: string, value: unknown, index: number) => unknown): object {
    const newObj: Partial<Record<string, unknown>> = {}
    objectForEach(obj, (key, value, i) => {
        newObj[key] = map(key, value, i)
    })
    return newObj
}


export function buildRecord<T, V>(array: T[], builder: (element: T, index: number) => [string, V]): Record<string, V> {
    const record: Partial<Record<string, V>> = {}
    let i = 0
    for (const element of array) {
        const [key, value] = builder(element, i)
        record[key] = value
        i++;
    }
    return record as Record<string, V>
}

export function removeBeforeLastExclusive  (string: string, removeBefore: string): string {
    const index = string.lastIndexOf(removeBefore)
    if (index === -1) {
        return string
    } else {
        return string.slice(index + removeBefore.length)
    }
}
// type X = Partial<any>

export function recordToArray<V, Rec extends Record<keyof Rec, V>, K extends keyof Rec, R>(record: Record<K, V>,
                                                                                           mapFn: (key: K, value: V, index: number) => R): R[] {
    return Object.keys(record).map((key, index) => mapFn(key as K, record[key as K] as V, index))
}
