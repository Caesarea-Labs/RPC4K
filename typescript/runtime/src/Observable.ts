/**
 * A handle received from an event method that allows subscribing to occurrences of the event by calling {@link observe}.
 * After observing is done, you should call {@link close} to tell the server we don't need this data anymore.
 */
export class Observable<T> {

    /**
     * @param listenerId A string associated with the subscription of this observable. This value may be passed to the server,
     * to tell it to refer to this exact observation. This is useful for example for excluding receiving certain events
     * for this particular observation.
     */
    constructor(public observe: (callback: (newValue: T) => void) => void, public close: () => void, public listenerId: string) {
    }

    map<R>(transform: (value: T) => R): Observable<R> {
        const newObserve: (callback: (newValue: R) => void) => void = (callback) => {
            this.observe(newValue => {
                callback(transform(newValue))
            })
        }
        return new Observable<R>(newObserve, this.close, this.listenerId)
    }
}