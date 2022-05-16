package tv.mycujoo.mcls.network.socket

import okhttp3.WebSocket
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class BFFRTListenerTest {

    @Mock
    lateinit var mBFFRTCallback: BFFRTCallback

    @Mock
    lateinit var webSocket: WebSocket

    @Test
    fun `given missing identifier should trigger missing identifier action`() {
        val listener = BFFRTListener(mBFFRTCallback)
        listener.onMessage(webSocket, "err;badRequest;sessionId;missingIdentifier")

        verify(mBFFRTCallback, times(1)).onBadRequest(BFFRTListener.BFFRtMessage.BAD_REQUEST.toString())
    }

    @Test
    fun `given forbidden response should trigger onForbidden`() {
        val listener = BFFRTListener(mBFFRTCallback)
        listener.onMessage(webSocket, "err;forbidden;identityToken;authFailed")

        verify(mBFFRTCallback, times(1)).onBadRequest(BFFRTListener.BFFRtMessage.BAD_REQUEST.toString())
    }

    @Test
    fun `given invalid command response should trigger onInvalidCommand`() {
        val listener = BFFRTListener(mBFFRTCallback)
        listener.onMessage(webSocket, "err;badRequest;-;invalidCommand")

        verify(mBFFRTCallback, times(1)).onBadRequest(BFFRTListener.BFFRtMessage.BAD_REQUEST.toString())
    }

    @Test
    fun `given no entitlement response should trigger onNoEntitlement`() {
        val listener = BFFRTListener(mBFFRTCallback)
        listener.onMessage(webSocket, "err;preconditionFailed;identityToken;notEntitled")

        verify(mBFFRTCallback, times(1)).onBadRequest(BFFRTListener.BFFRtMessage.BAD_REQUEST.toString())
    }

    @Test
    fun `given internal error response should trigger onInternalError`() {
        val listener = BFFRTListener(mBFFRTCallback)
        listener.onMessage(webSocket, "err;internal;identityToken;internalServerError")

        verify(mBFFRTCallback, times(1)).onServerError()
    }

    @Test
    fun `given generic not-expected response response should trigger onUnknownError`() {
        val listener = BFFRTListener(mBFFRTCallback)
        listener.onMessage(webSocket, "some_error")

        verify(mBFFRTCallback, times(1)).onBadRequest(BFFRTListener.BFFRtMessage.UNKNOWN_ERROR.toString())
    }

    @Test
    fun `given limit exceeded response should trigger onLimitExceeded`() {
        val listener = BFFRTListener(mBFFRTCallback)
        listener.onMessage(webSocket, "concurrencyLimitExceeded;2")

        verify(mBFFRTCallback, times(1)).onLimitExceeded(2)
    }

    @Test
    fun `given limit exceeded response with invalid number should trigger onLimitExceeded`() {
        val listener = BFFRTListener(mBFFRTCallback)
        listener.onMessage(webSocket, "concurrencyLimitExceeded;LIMIT")

        verify(mBFFRTCallback, times(1)).onLimitExceeded(-1)
    }
}