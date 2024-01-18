import {EventClient} from "../RpcClient"
import {GenericWebsocket, WebsocketState} from "./GenericWebsocket"
import {Observable} from "../Observable";

export class WebsocketEventClient implements EventClient {
    private messageListeners: Record<string, (message: string) => void> = {}
    /**
     * A minor array keeping track of function calls that happened before the connection was opened, and are waiting for it be opened.
     */
    private openListeners: (() => void)[] = []

    constructor(private socket: GenericWebsocket) {
        this.socket.listen({
            onError(error: string) {
                throw new Error(JSON.stringify(error))
            },
            onMessage: (message: string) => {
                //TODO: optimize to not rejoin the message
                const [type, listenerId, ...payload] = message.split(":")
                switch (type) {
                    case "event": {
                        const listener = this.messageListeners[listenerId]
                        if (listener !== undefined) {
                            listener(payload.join(":"))
                        } else {
                            console.warn(`Could not find listener for id '${listenerId}', the subscription may still open on the server`, message)
                        }
                        break
                    }
                    case "error": {
                        throw new Error(`Failed to subscribe to event: ${message.removePrefix("error:")}`)
                    }
                }
            },
            onClose(closeReason: string) {
                console.log(`Closing: `, closeReason)
            },
            onOpen: () => {
                console.log("WebSocket connection established.")
                for (const listener of this.openListeners) {
                    listener()
                }
                // The open listeners have been fulfilled
                this.openListeners = []
            }
        })
    }

    generateUuid(): string {
        return this.socket.generateUuid()
    }

    async send(message: string): Promise<void> {
        await this.waitForOpen().catch(e => {
            console.log(e)
        })
        this.socket.sendMessage(message)
    }

    createObservable(subscribeMessage: string, unsubscribeMessage: string, listenerId: string): Observable<string> {
        let observed = false
        return new Observable<string>(
            (callback: (newValue: string) => void) => {
                // Tell the server to update us about this event
                void this.send(subscribeMessage)

                // Register the given callback to be invoked whenever a new event is received
                this.messageListeners[listenerId] = callback
                observed = true
            },
            () => {
                if (observed) {
                    // Clean up callback listener
                    delete this.messageListeners[listenerId]
                    // Tell the server to not update us about this event anymore
                    void this.send(unsubscribeMessage)
                }
            }
        )
    }

    private async waitForOpen(): Promise<void> {
        if (this.socket.readyState === WebsocketState.CONNECTING) {
            return new Promise(resolve => {
                this.openListeners.push(() => {
                    resolve()
                })
            })
        } else if (this.socket.readyState === WebsocketState.OPEN) {
            // Do nothing
        } else {
            throw new Error("Attempt to send message when websocket is closed/closing.")
        }

    }
}


