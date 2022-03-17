package tv.mycujoo.mcls.network.socket

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import timber.log.Timber
import tv.mycujoo.mcls.di.ConcurrencySocketUrl
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BFFRTSocket @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val mainSocketListener: MainWebSocketListener,
    private val userPreferencesUtils: UserPreferencesUtils,
    @ConcurrencySocketUrl private val webSocketUrl: String,
) : IBFFRTSocket {

    private var webSocket: WebSocket? = null

    override fun addListener(BFFRTCallback: BFFRTCallback) {
        mainSocketListener.addListener(BFFRTListener(BFFRTCallback))
    }

    override fun startSession(eventId: String, identityToken: String?) {
        if (webSocket == null) {
            createSocket(eventId)
        }

        val requestMessage = if (identityToken == null) {
            "$SESSION_ID${userPreferencesUtils.getPseudoUserId()}"
        } else {
            "$SESSION_ID${userPreferencesUtils.getPseudoUserId()}$SEMICOLON$IDENTITY_TOKEN$identityToken"
        }

        webSocket?.send(requestMessage)
    }

    override fun leaveCurrentSession() {
        try {
            webSocket?.close(NORMAL_CLOSURE_STATUS_CODE, null)
        } catch (socketError: IllegalArgumentException) {
            Timber.e("Error Closing the Socket ${socketError.message}")
        }
        webSocket = null
    }

    private fun createSocket(eventId: String) {
        val request = Request.Builder().url("$webSocketUrl/event/$eventId").build()
        webSocket = okHttpClient.newWebSocket(request, mainSocketListener)
    }
}