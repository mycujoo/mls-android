package tv.mycujoo.mcls.network.socket

import okhttp3.OkHttpClient
import okhttp3.WebSocket
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
class BFFRTSocketTest {

    private lateinit var mainWebSocketListener: MainWebSocketListener
    private lateinit var BFFRTSocket: BFFRTSocket

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

        BFFRTSocket =
            BFFRTSocket(okHttpClient, mainWebSocketListener, userPreferencesUtils, "wss://bff-rt.mycujoo.tv")
    }

    @Test
    fun `given start session with no identity token should start session`() {
        whenever(userPreferencesUtils.getPseudoUserId()).thenReturn("pseudo_user_id")
        BFFRTSocket.startSession("123", null)

        verify(webSocket, atLeastOnce()).send("${DEVICE_ID}pseudo_user_id")
    }

    @Test
    fun `given start session with identity token should start session`() {
        whenever(userPreferencesUtils.getPseudoUserId()).thenReturn("pseudo_user_id")
        BFFRTSocket.startSession("123", "token")

        verify(webSocket, atLeastOnce()).send("${DEVICE_ID}pseudo_user_id$SEMICOLON${IDENTITY_TOKEN}token")
    }

    @Test
    fun `given leave session should leave session`() {
        whenever(userPreferencesUtils.getPseudoUserId()).thenReturn("pseudo_user_id")

        BFFRTSocket.startSession("123", null)
        BFFRTSocket.leaveCurrentSession()

        verify(webSocket, atLeastOnce()).close(NORMAL_CLOSURE_STATUS_CODE, null)
    }
}
