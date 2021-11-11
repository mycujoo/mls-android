package tv.mycujoo.mcls.network.socket

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import tv.mycujoo.mcls.model.JoinTimelineParam
import javax.inject.Inject

class ReactorSocket @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val mainSocketListener: MainWebSocketListener
) : IReactorSocket {


    private lateinit var webSocket: WebSocket

    private lateinit var uuid: String

    private var created = false
    private var connected = false
    private var joinedTimeline = false
    private lateinit var eventId: String
    private lateinit var timelineId: String
    private lateinit var updateId: String


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
    override fun joinEvent(eventId: String) {
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

            if (this::timelineId.isInitialized && joinedTimeline) {
                webSocket.send("$LEAVE_TIMELINE$timelineId")
                joinedTimeline = false
            }

            webSocket.send("$LEAVE_EVENT$eventId")
            connected = false

            if (destroyAfter) {
                destroySocket()
            }
        }
    }

    /**
     * Join a timeline to listen to timeline changes
     * before doing so, a connection must be active
     * @param param provides timelineId & lastActionId (optional)
     */
    override fun joinTimeline(param: JoinTimelineParam) {
        if (created.not() || connected.not()) {
            return
        }

        if (this::timelineId.isInitialized && timelineId != param.timelineId) {
            leaveTimeline()
        }

        if (param.lastActionId != null) {
            timelineId = param.timelineId
            updateId = param.lastActionId
            webSocket.send("$JOIN_TIMELINE${param.timelineId}$SEMICOLON${param.lastActionId}")

        } else {
            timelineId = param.timelineId
            webSocket.send("$JOIN_TIMELINE${param.timelineId}$SEMICOLON")

        }
        joinedTimeline = true

    }


    fun leaveTimeline() {
        if (this::timelineId.isInitialized && joinedTimeline) {
            webSocket.send("$LEAVE_TIMELINE$timelineId")
            joinedTimeline = false
        }
    }

    private fun createSocket() {
        val request = Request.Builder().url(WEB_SOCKET_URL).build()
        webSocket = okHttpClient.newWebSocket(request, mainSocketListener)
        created = true

        webSocket.send("$SESSION_ID$uuid")
    }


    private fun destroySocket() {
        webSocket.close(NORMAL_CLOSURE_STATUS_CODE, null)
        created = false
    }
}