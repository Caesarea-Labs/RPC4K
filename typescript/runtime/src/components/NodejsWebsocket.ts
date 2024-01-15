import {WebSocket} from "ws"
import {GenericWebsocket, WebsocketListeners} from "./GenericWebsocket"
import {v4} from "uuid"

export class NodejsWebsocket implements GenericWebsocket {
    private socket: WebSocket
    readyState

    constructor(url: string) {
        this.socket = new WebSocket(url)
        this.readyState = this.socket.readyState
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

