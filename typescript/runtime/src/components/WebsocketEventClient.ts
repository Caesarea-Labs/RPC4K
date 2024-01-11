import {Json} from "../serialization/json/Json"

export class WebSocketClient {
    private messageListeners: Record<string, (message: string) => void> = {}
    /**
     * A minor array keeping track of function calls that happened before the connection was opened, and are waiting for it be opened.
     */
    private openListeners: (() => void)[] = []
    socket: WebSocket = null!

    constructor(private url: string, active?: boolean) {
        if (active === false) return
        this.socket = new WebSocket(this.url)
        this.socket.onopen = () => {
            console.log("WebSocket connection established.")
            for (const listener of this.openListeners) {
                listener()
            }
            // The open listeners have been fulfilled
            this.openListeners = []
        }
        this.socket.onerror = (error) => {
            throw new Error(JSON.stringify(error))
        }
        this.socket.onclose = (event) => {
            console.log(`Closing: `, event)
        }
        this.socket.onmessage = (event) => {
            // console.log(`Got message: ${event.data}`)
            const message = event.data as string
            //TODO: optimize to not rejoin the message
            const [type, listenerId, ...payload] = message.split(":")
            switch (type) {
                case "event": {
                    const listener = this.messageListeners[listenerId]
                    if (listener !== undefined) {
                        listener(payload.join(":"))
                    } else {
                        console.warn(`Could not find listener for id '${listenerId}', is the subscription still open on the server?`, message)
                    }
                    break
                }
                case "error": {
                    throw new Error(`Failed to subscribe to event: ${message.removePrefix("error:")}`)
                }
            }

        }
    }


    private async sendMessage(message: string): Promise<void> {
        await this.waitForOpen().catch(e => {
            console.log(e)
        })
        this.socket.send(message)
    }

    private async waitForOpen(): Promise<void> {
        if (this.socket.readyState === WebSocket.CONNECTING) {
            return new Promise(resolve => {
                this.openListeners.push(() => {
                    resolve()
                })
            })
        } else if (this.socket.readyState === WebSocket.OPEN) {
            // Do nothing
        } else {
            throw new Error("Attempt to send message when websocket is closed/closing.")
        }

    }

    listen(event: string, watchedObjectId: string | undefined, args: unknown): Observable<string> {
        const id = crypto.randomUUID()
        let observed = false
        return new Observable<string>(
            (callback: (newValue: string) => void) => {
                // Tell the server to update us about this event
                void this.sendMessage(`sub:${event}:${id}:${watchedObjectId ?? ""}:${JSON.stringify(args)}`)

                // Register the given callback to be invoked whenever a new event is received
                this.messageListeners[id] = callback
                observed = true
            },
            () => {
                if (observed) {
                    // Clean up callback listener
                    delete this.messageListeners[id]
                    // Tell the server to not update us about this event anymore
                    void this.sendMessage(`unsub:${event}:${id}`)
                }
            }
        )
    }

    json = new Json()

    subscribeToActionHistory(query: string, page: number): Observable<TestEventResponse> {
        return this.listen("subscribeToActionHistory", undefined, [query, page])
            .map(value => ({}))
    }
}

interface TestEventResponse {

}


class Observable<T> {
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
