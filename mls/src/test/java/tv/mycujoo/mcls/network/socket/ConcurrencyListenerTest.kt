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
    lateinit var concurrencyCallback: ConcurrencyCallback

    @Mock
    lateinit var webSocket: WebSocket

    @Test
    fun `given ok response should trigger onOk`() {
        val listener = ConcurrencyListener(concurrencyCallback)
        listener.onMessage(webSocket, "ok;identityToken")

        verify(concurrencyCallback, times(1)).onOK()
    }

    @Test
    fun `given missing identifier should trigger missing identifier action`() {
        val listener = ConcurrencyListener(concurrencyCallback)
        listener.onMessage(webSocket, "err;badRequest;sessionId;missingIdentifier")

        verify(concurrencyCallback, times(1)).onMissingIdentifier()
    }

    @Test
    fun `given forbidden response should trigger onForbidden`() {
        val listener = ConcurrencyListener(concurrencyCallback)
        listener.onMessage(webSocket, "err;forbidden;identityToken;authFailed")

        verify(concurrencyCallback, times(1)).onForbidden()
    }

    @Test
    fun `given invalid command response should trigger onInvalidCommand`() {
        val listener = ConcurrencyListener(concurrencyCallback)
        listener.onMessage(webSocket, "err;badRequest;-;invalidCommand")

        verify(concurrencyCallback, times(1)).onInvalidCommand()
    }

    @Test
    fun `given no entitlement response should trigger onNoEntitlement`() {
        val listener = ConcurrencyListener(concurrencyCallback)
        listener.onMessage(webSocket, "err;preconditionFailed;identityToken;notEntitled")

        verify(concurrencyCallback, times(1)).onNoEntitlement()
    }

    @Test
    fun `given internal error response should trigger onInternalError`() {
        val listener = ConcurrencyListener(concurrencyCallback)
        listener.onMessage(webSocket, "err;internal;identityToken;internalServerError")

        verify(concurrencyCallback, times(1)).onInternalError()
    }

    @Test
    fun `given generic not-expected response response should trigger onUnknownError`() {
        val listener = ConcurrencyListener(concurrencyCallback)
        listener.onMessage(webSocket, "some_error")

        verify(concurrencyCallback, times(1)).onUnknownError()
    }

    @Test
    fun `given limit exceeded response should trigger onLimitExceeded`() {
        val listener = ConcurrencyListener(concurrencyCallback)
        listener.onMessage(webSocket, "concurrencyLimitExceeded;LIMIT")

        verify(concurrencyCallback, times(1)).onLimitExceeded()
    }
}