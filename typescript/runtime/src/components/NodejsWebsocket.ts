import {WebSocket} from "ws"
import {GenericWebsocket, WebsocketListeners, WebsocketReadyState} from "./GenericWebsocket"
import {v4} from "uuid"

export class NodejsWebsocket implements GenericWebsocket {
    private socket: WebSocket

    getReadyState(): WebsocketReadyState {
        return this.socket.readyState
    }

    constructor(public url: string) {
        this.socket = new WebSocket(url)
    }

    close(): void {
        this.socket.close()
    }

    generateUuid(): string {
        return v4()
    }

    listen(listeners: WebsocketListeners): void {
        this.socket.onopen = () => listeners.onOpen()
        this.socket.onclose = (event) => listeners.onClose(JSON.stringify(event))
        this.socket.onerror = (event) => listeners.onError(JSON.stringify(event))
        this.socket.onmessage = (event) => listeners.onMessage(event.data as string)
    }
    sendMessage(message: string): void {
        this.socket.send(message)
    }
}

