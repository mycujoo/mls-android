package tv.mycujoo.mcls.network.socket

import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import java.lang.NumberFormatException

class BFFRTListener constructor(
    private val BFFRTCallback: BFFRTCallback
) : WebSocketListener() {

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)

        Timber.d("${webSocket.request()} $text")

        when (parseMessage(text)) {
            BFFRtMessage.CONCURRENCY_LIMIT_EXCEEDED -> {
                val responses = text.split(";")
                try {
                    BFFRTCallback.onLimitExceeded(responses[1].toInt())
                } catch (numberFormatException: NumberFormatException) {
                    BFFRTCallback.onLimitExceeded(-1)
                }
            }
            BFFRtMessage.BAD_REQUEST -> BFFRTCallback.onBadRequest(BFFRtMessage.BAD_REQUEST.toString())
            BFFRtMessage.INTERNAL_ERROR -> BFFRTCallback.onServerError()
            else -> BFFRTCallback.onBadRequest(BFFRtMessage.UNKNOWN_ERROR.toString())
        }
    }

    private fun parseMessage(message: String): BFFRtMessage {
        val responses = message.split(";")

        // Bad Response
        if (responses.size < 2) {
            return BFFRtMessage.UNKNOWN_ERROR
        }

        // Limit Exceeded
        if(responses[0] == "concurrencyLimitExceeded") {
            return BFFRtMessage.CONCURRENCY_LIMIT_EXCEEDED
        }

        // Errors
        if (responses[0] != "err") {
            return BFFRtMessage.UNKNOWN_ERROR
        }

        return when (responses[1]) {
            // Bad Requests Needs to be handled immediately.
            "badRequest" -> {
                BFFRtMessage.BAD_REQUEST
            }
            // Just Need the error to indicate the need for login, no debugging required.
            "forbidden" -> {
                BFFRtMessage.BAD_REQUEST
            }
            // Just Need the error to indicate that the event is not playable for this user, no debugging required.
            "preconditionFailed" -> {
                BFFRtMessage.BAD_REQUEST
            }
            // This is an indicator that could be used in crashlytics as an early warning.
            "internal" -> {
                BFFRtMessage.INTERNAL_ERROR
            }
            else -> BFFRtMessage.UNKNOWN_ERROR
        }
    }

    enum class BFFRtMessage {
        CONCURRENCY_LIMIT_EXCEEDED,
        BAD_REQUEST,
        INTERNAL_ERROR,
        UNKNOWN_ERROR
    }
}