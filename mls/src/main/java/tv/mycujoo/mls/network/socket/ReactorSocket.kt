package tv.mycujoo.mls.network.socket

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

class ReactorSocket(private val okHttpClient: OkHttpClient, private val mainSocketListener: MainWebSocketListener) :
    IReactorSocket {


    private lateinit var webSocket: WebSocket

    private lateinit var uuid: String

    private var created = false
    private var connected = false
    private lateinit var eventId: String


    override fun addListener(reactorCallback: ReactorCallback) {
        mainSocketListener.addListener(ReactorListener(reactorCallback))
    }


    /**
     * Must be called before any usage!
     * @param uuid must be persisted on phones storage to be unique
     */
    override fun setUUID(uuid: String) {
        this.uuid = uuid
    }

    /**
     * Joins to an Event with eventId by sending JOIN command.
     * before doing so, it checks if active connection is already established,
     * if so it will terminate that.
     * Also, it checks if a socket client has been created,
     * if not, it will create one.
     *  @param eventId
     */
    override fun join(eventId: String) {
        if (this::uuid.isInitialized.not()) {
            throw UninitializedPropertyAccessException("uuid must be initialized")
        }
        if (connected) {
            leave(false)
        }

        if (created.not()) {
            createSocket()
        }

        this.eventId = eventId
        if (this::webSocket.isInitialized.not()) {
            Log.e(ReactorSocket::class.java.canonicalName, "webSocket must be initialized")
            return
        }
        webSocket.send("$JOIN_EVENT$eventId")
        connected = true
    }

    /**
     * Leaves a connection by sending LEAVE command.
     * eventId from formerly used join command will be used
     * @param destroyAfter will decide if socket should be terminated after leaving.
     */
    override fun leave(destroyAfter: Boolean) {
        if (created && connected) {
            webSocket.send("$LEAVE_EVENT$eventId")
            connected = false

            if (destroyAfter) {
                destroySocket()
            }
        }
    }


    private fun createSocket() {
        val request = Request.Builder().url("wss://mls-rt.mycujoo.tv").build()
        webSocket = okHttpClient.newWebSocket(request, mainSocketListener)
        created = true

        webSocket.send("$SESSION_ID$uuid")
    }


    private fun destroySocket() {
        webSocket.close(NORMAL_CLOSURE_STATUS_CODE, null)
        created = false
    }
}