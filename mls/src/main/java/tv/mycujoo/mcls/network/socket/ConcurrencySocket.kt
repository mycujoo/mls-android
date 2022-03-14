package tv.mycujoo.mcls.network.socket

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConcurrencySocket @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val mainSocketListener: MainWebSocketListener,
    private val userPreferencesUtils: UserPreferencesUtils,
) : IConcurrencySocket {

    private lateinit var webSocket: WebSocket
    var created = false

    override fun addListener(concurrencyCallback: ConcurrencyCallback) {
        mainSocketListener.addListener(ConcurrencyListener(concurrencyCallback))
    }

    override fun startSession(eventId: String, identityToken: String?) {
        if (created.not()) {
            createSocket(eventId)
        }

        val requestMessage = if (identityToken == null) {
            "$SESSION_ID${userPreferencesUtils.getPseudoUserId()}"
        } else {
            "$SESSION_ID${userPreferencesUtils.getPseudoUserId()}$SEMICOLON$IDENTITY_TOKEN$identityToken"
        }

        webSocket.send(requestMessage)
    }

    override fun leaveCurrentSession() {
        webSocket.close(NORMAL_CLOSURE_STATUS_CODE, null)
        created = false
    }

    private fun createSocket(eventId: String) {
        val request = Request.Builder().url("$CONCURRENCY_WEB_SOCKET_URL$eventId").build()
        webSocket = okHttpClient.newWebSocket(request, mainSocketListener)
        created = true
    }
}