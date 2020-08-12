package tv.mycujoo.mls.network.socket

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

class ReactorSocket(okHttpClient: OkHttpClient, private val mainSocketListener: MainWebSocketListener) : IReactorSocket {


    private var webSocket: WebSocket

    init {
        val request = Request.Builder().url("wss://mls-rt.mycujoo.tv").build()
        webSocket = okHttpClient.newWebSocket(request, mainSocketListener)
    }

    override fun addListener(reactorCallback: ReactorCallback) {
        mainSocketListener.addListener(ReactorListener(reactorCallback))
    }


    override fun connect(eventId: String) {
        webSocket.send("joinEvent;$eventId")
    }

    override fun disconnect(eventId: String) {
        webSocket.send("leaveEvent;$eventId")
    }


}