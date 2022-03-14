package tv.mycujoo.mcls.network.socket

import okhttp3.WebSocket
import okhttp3.WebSocketListener
import tv.mycujoo.mcls.enum.MessageLevel
import tv.mycujoo.mcls.manager.Logger

class ConcurrencyListener constructor(
    private val concurrencyCallback: ConcurrencyCallback
) : WebSocketListener() {

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)

        when (parseMessage(text)) {
            ConcurrencyMessage.OK -> concurrencyCallback.onOK()
            ConcurrencyMessage.CONCURRENCY_LIMIT_EXCEEDED -> concurrencyCallback.onLimitExceeded()
            ConcurrencyMessage.MISSING_IDENTIFIER -> concurrencyCallback.onMissingIdentifier()
            ConcurrencyMessage.FORBIDDEN -> concurrencyCallback.onForbidden()
            ConcurrencyMessage.NOT_ENTITLED -> concurrencyCallback.onNoEntitlement()
            ConcurrencyMessage.INTERNAL_ERROR -> concurrencyCallback.onInternalError()
            ConcurrencyMessage.INVALID_COMMAND -> concurrencyCallback.onInvalidCommand()
            ConcurrencyMessage.UNKNOWN_ERROR -> concurrencyCallback.onUnknownError()
        }
    }

    private fun parseMessage(message: String): ConcurrencyMessage {
        val responses = message.split(";")

        // Bad Response
        if (responses.size < 2) {
            return ConcurrencyMessage.UNKNOWN_ERROR
        }

        // OK Response
        if (responses[0].lowercase() == "ok" && responses.size == 2) {
            return ConcurrencyMessage.OK
        }

        // Limit Exceeded
        if(responses[0].lowercase() == "concurrencyLimitExceeded") {
            return ConcurrencyMessage.CONCURRENCY_LIMIT_EXCEEDED
        }

        // Errors
        if (responses[0] != "err") {
            return ConcurrencyMessage.UNKNOWN_ERROR
        }

        return when (responses[1]) {
            // Bad Requests Needs to be handled immediately.
            "badRequest" -> {
                when {
                    responses[3] == "invalidCommand" -> {
                        ConcurrencyMessage.INVALID_COMMAND
                    }
                    responses[3] == "missingIdentifier" -> {
                        ConcurrencyMessage.MISSING_IDENTIFIER
                    }
                    else -> {
                        ConcurrencyMessage.UNKNOWN_ERROR
                    }
                }
            }
            // Just Need the error to indicate the need for login, no debugging required.
            "forbidden" -> {
                ConcurrencyMessage.FORBIDDEN
            }
            // Just Need the error to indicate that the event is not playable for this user, no debugging required.
            "preconditionFailed" -> {
                ConcurrencyMessage.NOT_ENTITLED
            }
            // This is an indicator that could be used in crashlytics as an early warning.
            "internal" -> {
                ConcurrencyMessage.INTERNAL_ERROR
            }
            else -> ConcurrencyMessage.UNKNOWN_ERROR
        }
    }

    enum class ConcurrencyMessage {
        OK,
        CONCURRENCY_LIMIT_EXCEEDED,
        INVALID_COMMAND,
        MISSING_IDENTIFIER,
        FORBIDDEN,
        NOT_ENTITLED,
        INTERNAL_ERROR,
        UNKNOWN_ERROR
    }
}