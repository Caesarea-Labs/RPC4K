export function recordForEach<K extends keyof object, V>(record: Record<K, V>, iterator: (key: K, value: V) => void) {
    for (const key in record) {
        iterator(key, record[key])
    }
}