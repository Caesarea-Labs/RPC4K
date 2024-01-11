import {Json} from "../serialization/json/Json"
import {EventClient, Observable} from "../RpcClient"

export class WebSocketEventClient implements EventClient {
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

    async send(message: string): Promise<void> {
        await this.waitForOpen().catch(e => {
            console.log(e)
        })
        this.socket.send(message)
    }

    createObservable(subscribeMessage: string, unsubscribeMessage: string, listenerId: string): Observable<string> {
        let observed = false
        return new Observable<string>(
            (callback: (newValue: string) => void) => {
                // Tell the server to update us about this event
                void this.sendMessage(subscribeMessage)

                // Register the given callback to be invoked whenever a new event is received
                this.messageListeners[listenerId] = callback
                observed = true
            },
            () => {
                if (observed) {
                    // Clean up callback listener
                    delete this.messageListeners[listenerId]
                    // Tell the server to not update us about this event anymore
                    void this.sendMessage(unsubscribeMessage)
                }
            }
        )
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
}


