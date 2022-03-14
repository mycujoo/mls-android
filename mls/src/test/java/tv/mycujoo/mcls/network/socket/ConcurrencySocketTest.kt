package tv.mycujoo.mcls.network.socket

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import tv.mycujoo.mcls.utils.UserPreferencesUtils

@RunWith(MockitoJUnitRunner::class)
class ConcurrencySocketTest {

    private lateinit var mainWebSocketListener: MainWebSocketListener
    private lateinit var concurrencySocket: ConcurrencySocket

    @Mock
    lateinit var okHttpClient: OkHttpClient

    @Mock
    lateinit var webSocket: WebSocket

    @Mock
    lateinit var userPreferencesUtils: UserPreferencesUtils

    @Before
    fun setup() {
        mainWebSocketListener = MainWebSocketListener()
        whenever(okHttpClient.newWebSocket(any(), any())).thenReturn(webSocket)

        concurrencySocket =
            ConcurrencySocket(okHttpClient, mainWebSocketListener, userPreferencesUtils)
    }

    @Test
    fun `given start session with no identity token should start session`() {
        whenever(userPreferencesUtils.getPseudoUserId()).thenReturn("pseudo_user_id")
        concurrencySocket.startSession("123", null)

        verify(webSocket, atLeastOnce()).send("${SESSION_ID}pseudo_user_id")
    }

    @Test
    fun `given start session with identity token should start session`() {
        whenever(userPreferencesUtils.getPseudoUserId()).thenReturn("pseudo_user_id")
        concurrencySocket.startSession("123", "token")

        verify(webSocket, atLeastOnce()).send("${SESSION_ID}pseudo_user_id$SEMICOLON${IDENTITY_TOKEN}token")
    }

    @Test
    fun `given leave session should leave session`() {
        whenever(userPreferencesUtils.getPseudoUserId()).thenReturn("pseudo_user_id")

        concurrencySocket.startSession("123", null)
        concurrencySocket.leaveCurrentSession()

        verify(webSocket, atLeastOnce()).close(NORMAL_CLOSURE_STATUS_CODE, null)
    }
}
