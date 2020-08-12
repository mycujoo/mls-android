package tv.mycujoo.mls.network.socket

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class MainWebSocketListener : WebSocketListener() {


    private var socketListeners = mutableListOf<WebSocketListener>()

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        for (socketListener in socketListeners) {
            socketListener.onMessage(webSocket, text)
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        for (socketListener in socketListeners) {
            socketListener.onMessage(webSocket, bytes)
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
    }

    fun addListener(webSocketListener: WebSocketListener) {
        socketListeners.add(webSocketListener)
    }
}