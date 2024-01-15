export class Observable<T> {
    constructor(public observe: (callback: (newValue: T) => void) => void, public close: () => void) {
    }

    map<R>(transform: (value: T) => R): Observable<R> {
        const newObserve: (callback: (newValue: R) => void) => void = (callback) => {
            this.observe(newValue => {
                callback(transform(newValue))
            })
        }
        return new Observable<R>(newObserve, this.close)
    }
}