package tv.mycujoo.mcls.network.socket

import okhttp3.WebSocket
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class ConcurrencyListenerTest {

    @Mock
    lateinit var BFFRTCallback: BFFRTCallback

    @Mock
    lateinit var webSocket: WebSocket

    @Test
    fun `given missing identifier should trigger missing identifier action`() {
        val listener = BFFRTListener(BFFRTCallback)
        listener.onMessage(webSocket, "err;badRequest;sessionId;missingIdentifier")

        verify(BFFRTCallback, times(1)).onBadRequest(BFFRTListener.BFFRtMessage.BAD_REQUEST.toString())
    }

    @Test
    fun `given forbidden response should trigger onForbidden`() {
        val listener = BFFRTListener(BFFRTCallback)
        listener.onMessage(webSocket, "err;forbidden;identityToken;authFailed")

        verify(BFFRTCallback, times(1)).onBadRequest(BFFRTListener.BFFRtMessage.BAD_REQUEST.toString())
    }

    @Test
    fun `given invalid command response should trigger onInvalidCommand`() {
        val listener = BFFRTListener(BFFRTCallback)
        listener.onMessage(webSocket, "err;badRequest;-;invalidCommand")

        verify(BFFRTCallback, times(1)).onBadRequest(BFFRTListener.BFFRtMessage.BAD_REQUEST.toString())
    }

    @Test
    fun `given no entitlement response should trigger onNoEntitlement`() {
        val listener = BFFRTListener(BFFRTCallback)
        listener.onMessage(webSocket, "err;preconditionFailed;identityToken;notEntitled")

        verify(BFFRTCallback, times(1)).onBadRequest(BFFRTListener.BFFRtMessage.BAD_REQUEST.toString())
    }

    @Test
    fun `given internal error response should trigger onInternalError`() {
        val listener = BFFRTListener(BFFRTCallback)
        listener.onMessage(webSocket, "err;internal;identityToken;internalServerError")

        verify(BFFRTCallback, times(1)).onServerError()
    }

    @Test
    fun `given generic not-expected response response should trigger onUnknownError`() {
        val listener = BFFRTListener(BFFRTCallback)
        listener.onMessage(webSocket, "some_error")

        verify(BFFRTCallback, times(1)).onBadRequest(BFFRTListener.BFFRtMessage.BAD_REQUEST.toString())
    }

    @Test
    fun `given limit exceeded response should trigger onLimitExceeded`() {
        val listener = BFFRTListener(BFFRTCallback)
        listener.onMessage(webSocket, "concurrencyLimitExceeded;LIMIT")

        verify(BFFRTCallback, times(1)).onLimitExceeded()
    }
}