package tv.mycujoo.mcls.network.socket

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.ByteString
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import tv.mycujoo.mcls.utils.ThreadUtils
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import java.lang.Exception

@RunWith(MockitoJUnitRunner::class)
class BFFRTSocketTest {

    private lateinit var mainWebSocketListener: MainWebSocketListener
    private lateinit var mBFFRTSocket: BFFRTSocket

    @Mock
    lateinit var okHttpClient: OkHttpClient

    @Mock
    lateinit var webSocket: WebSocket

    @Mock
    lateinit var userPreferencesUtils: UserPreferencesUtils

    private val MOCK_WEB_SERVER_PORT = 8004

    @Before
    fun setup() {
        mainWebSocketListener = MainWebSocketListener()
        whenever(okHttpClient.newWebSocket(any(), any())).thenReturn(webSocket)

        mBFFRTSocket =
            BFFRTSocket(
                okHttpClient,
                mainWebSocketListener,
                userPreferencesUtils,
                "http://localhost:$MOCK_WEB_SERVER_PORT/",
                ThreadUtils()
            )
    }

    @Test
    fun `given start session with no identity token should start session`() {
        whenever(userPreferencesUtils.getPseudoUserId()).thenReturn("pseudo_user_id")
        mBFFRTSocket.startSession("123", null)

        verify(webSocket, atLeastOnce()).send("${DEVICE_ID}pseudo_user_id")
    }

    @Test
    fun `given start session with identity token should start session`() {
        whenever(userPreferencesUtils.getPseudoUserId()).thenReturn("pseudo_user_id")
        mBFFRTSocket.startSession("123", "token")

        verify(webSocket, times(1))
            .send("${DEVICE_ID}pseudo_user_id")
        verify(webSocket, times(1))
            .send("${IDENTITY_TOKEN}token")
    }

    @Test
    fun `given leave session should leave session`() {
        whenever(userPreferencesUtils.getPseudoUserId()).thenReturn("pseudo_user_id")

        mBFFRTSocket.startSession("123", null)
        mBFFRTSocket.leaveCurrentSession()

        verify(webSocket, atLeastOnce()).close(NORMAL_CLOSURE_STATUS_CODE, null)
    }

    @Test
    fun `given leaveCurrentSession should close network`(): Unit = runBlocking {
        val mockWebServer = MockWebServer()
        mockWebServer.start(MOCK_WEB_SERVER_PORT)
        whenever(userPreferencesUtils.getPseudoUserId()).thenReturn("pseudo_user_id")
        var socketOpened: Boolean? = null

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        super.onClosing(webSocket, code, reason)
                        println("onClosing $code $reason")
                        webSocket.close(code, reason)
                    }

                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?
                    ) {
                        super.onFailure(webSocket, t, response)
                        println("onFailure")
                    }

                    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                        super.onMessage(webSocket, bytes)
                        println("onMessage")
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        super.onMessage(webSocket, text)
                        println("onMessage")
                    }

                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)
                        println("onOpen")
                        socketOpened = true
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        super.onClosed(webSocket, code, reason)
                        println("onClosed")
                        socketOpened = false
                    }
                })
            }
        }

        val bffRtSocket = BFFRTSocket(
            okHttpClient = OkHttpClient(),
            mainSocketListener = MainWebSocketListener(),
            userPreferencesUtils = userPreferencesUtils,
            webSocketUrl = mockWebServer.url("/").toString(),
            ThreadUtils()
        )

        bffRtSocket.startSession("123", "id_token")
        bffRtSocket.leaveCurrentSession()

        println(mockWebServer.url("/").toString())

        delay(50)

        socketOpened shouldBeEqualTo false

        mockWebServer.shutdown()
    }

    @Test
    fun `given leaveCurrentSession did not get close network from the server, it should force close`(): Unit =
        runBlocking {
            val mockWebServer = MockWebServer()
            mockWebServer.start(MOCK_WEB_SERVER_PORT)

            whenever(userPreferencesUtils.getPseudoUserId()).thenReturn("pseudo_user_id")

            mockWebServer.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    return MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                    })
                }
            }

            val bffRtSocket = BFFRTSocket(
                okHttpClient = OkHttpClient(),
                mainSocketListener = MainWebSocketListener(),
                userPreferencesUtils = userPreferencesUtils,
                webSocketUrl = mockWebServer.url("/").toString(),
                ThreadUtils()
            )

            bffRtSocket.startSession("123", "id_token")
            bffRtSocket.leaveCurrentSession()

            delay(1000)

            try {
                mockWebServer.shutdown()
            } catch (e: Exception) {
            }
        }
}
