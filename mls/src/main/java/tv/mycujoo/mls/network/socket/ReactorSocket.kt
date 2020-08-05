package tv.mycujoo.mls.network.socket

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

class ReactorSocket(private val okHttpClient: OkHttpClient) {


    private lateinit var webSocket: WebSocket

    fun initialize(reactorListener: ReactorListener) {
        val request = Request.Builder().url("wss://mls-rt.mycujoo.tv").build()
        webSocket = okHttpClient.newWebSocket(request, reactorListener)
    }


    fun connect(eventId: String) {
        if (this::webSocket.isInitialized.not()) {
            return
        }

        webSocket.send("joinEvent;$eventId")
    }

}