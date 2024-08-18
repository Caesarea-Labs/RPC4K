import {EventClient} from "../RpcClient"
import {GenericWebsocket, WebsocketState} from "./GenericWebsocket"
import {Observable} from "../Observable"
import {wait} from "../impl/Utils"

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
                //SLOW: optimize to not rejoin the message
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
        // console.log(`Waiting for open for message ${message}`)
        await this.waitForOpen().catch(e => {
            console.log(e)
        })
        // console.log(`Sending message ${message}`)
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
                // console.log(`Closing observable with listenerId of ${listenerId} and observed of ${observed}`)
                if (observed) {
                    // Clean up callback listener
                    delete this.messageListeners[listenerId]
                    // Tell the server to not update us about this event anymore
                    void this.send(unsubscribeMessage)
                }
            },
            listenerId
        )
    }

    private async waitForOpen(): Promise<void> {
        // while (true) {
        if (this.socket.getReadyState() === WebsocketState.CONNECTING) {
            // console.log("Connecting")
            // await wait(1000)
            return new Promise(resolve => {
                this.openListeners.push(() => {
                    // console.log(`New state: ${this.socket.readyState}`)
                    resolve()
                })
            })
        } else if (this.socket.getReadyState() === WebsocketState.OPEN) {
            return  // Open - we can stop waiting
        } else {
            console.log("Error...")
            throw new Error("Attempt to send message when websocket is closed/closing.")
        }
    }


    // }
}


