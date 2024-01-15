import {WebSocket} from "ws"

export interface GenericWebsocket {
    listen(listeners: WebsocketListeners): void
    sendMessage(message: string): void
    readyState: WebsocketReadyState
    generateUuid(): string
}

export type WebsocketReadyState = 0 | 1 | 2 | 3

export namespace WebsocketState {
    export const CONNECTING = 0
    export const OPEN = 1
    export const CLOSING = 2
    export const CLOSED = 3
}

//     /** The connection is not yet open. */
//     static readonly CONNECTING: 0;
//     /** The connection is open and ready to communicate. */
//     static readonly OPEN: 1;
//     /** The connection is in the process of closing. */
//     static readonly CLOSING: 2;
//     /** The connection is closed. */
//     static readonly CLOSED: 3;

export interface WebsocketListeners {
    onOpen(): void
    onClose(closeReason: string): void
    onMessage(message: string): void
    onError(error: string): void
}