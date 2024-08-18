// noinspection DuplicatedCode

import {GenericWebsocket, WebsocketListeners, WebsocketReadyState} from "./GenericWebsocket"

export class BrowserWebsocket implements GenericWebsocket {
    private socket: WebSocket

    getReadyState(): WebsocketReadyState {
        return this.socket.readyState as WebsocketReadyState
    }

    constructor(public url: string) {
        this.socket = new WebSocket(url)
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
    close(): void {
        this.socket.close()
    }
}

