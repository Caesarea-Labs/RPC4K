export function recordForEach<K extends keyof object, V>(record: Record<K, V>, iterator: (key: K, value: V) => void) {
    for (const key in record) {
        iterator(key, record[key])
    }
}
export function objectForEach(obj: object, iterator: (key: string, value: unknown) => void) {
    for (const key in obj) {
        // @ts-ignore
        iterator(key, obj[key])
    }
}
export function objectMap(obj: object, map: (key: string, value: unknown) => unknown): object {
    const newObj: Partial<Record<string, unknown>> = {}
    objectForEach(obj, (key, value) => {
        newObj[key] = map(key, value)
    })
    return newObj
}