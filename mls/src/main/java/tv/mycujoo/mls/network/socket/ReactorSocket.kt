package tv.mycujoo.mls.network.socket

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

class ReactorSocket(okHttpClient: OkHttpClient, private val mainSocketListener: MainWebSocketListener) :
    IReactorSocket {


    private var webSocket: WebSocket
    private var connected = false
    private lateinit var eventId: String

    init {
        val request = Request.Builder().url("wss://mls-rt.mycujoo.tv").build()
        webSocket = okHttpClient.newWebSocket(request, mainSocketListener)
    }

    override fun addListener(reactorCallback: ReactorCallback) {
        mainSocketListener.addListener(ReactorListener(reactorCallback))
    }


    override fun connect(eventId: String) {
        if (connected) {
            disconnect(this.eventId)
        }
        this.eventId = eventId
        webSocket.send("$JOIN_EVENT$eventId")
        connected = true
    }

    override fun disconnect(eventId: String) {
        webSocket.send("$LEAVE_EVENT$eventId")
        connected = false
    }


}