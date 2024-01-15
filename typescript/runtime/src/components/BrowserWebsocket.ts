// noinspection DuplicatedCode

import {GenericWebsocket, WebsocketListeners, WebsocketReadyState} from "./GenericWebsocket"

export class BrowserWebsocket implements GenericWebsocket {
    private socket: WebSocket

    readyState: WebsocketReadyState

    constructor(url: string) {
        this.socket = new WebSocket(url)
        this.readyState = this.socket.readyState as WebsocketReadyState
    }

    generateUuid(): string {
        return crypto.randomUUID()
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

