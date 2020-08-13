package tv.mycujoo.mls.network.socket

import com.nhaarman.mockitokotlin2.*
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ReactorSocketTest {

    private lateinit var reactorSocket: ReactorSocket
    private lateinit var mainWebSocketListener: MainWebSocketListener

    @Mock
    lateinit var okHttpClient: OkHttpClient

    @Mock
    lateinit var webSocket: WebSocket

    @Mock
    lateinit var reactorCallback: ReactorCallback

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(okHttpClient.newWebSocket(any(), any())).thenReturn(webSocket)

        mainWebSocketListener = MainWebSocketListener()
        reactorSocket = ReactorSocket(okHttpClient, mainWebSocketListener)
        reactorSocket.addListener(reactorCallback)
    }

    @Test
    fun `given connect command, should connect to webSocket`() {
        reactorSocket.connect(EVENT_ID)


        verify(webSocket).send("joinEvent;$EVENT_ID")
    }

    @Test
    fun `given disconnect command, should disconnect from webSocket`() {
        reactorSocket.disconnect(EVENT_ID)


        verify(webSocket).send("leaveEvent;$EVENT_ID")
    }

    @Test
    fun `given connect command when active connection exist, should disconnect from active connection`() {
        reactorSocket.connect(EVENT_ID)


        reactorSocket.connect(EVENT_ID_NEW)


        verify(webSocket).send("leaveEvent;$EVENT_ID")
    }

    @Test
    fun `given connect command after disconnecting, should not disconnect again`() {
        reactorSocket.connect(EVENT_ID)
        reactorSocket.disconnect(EVENT_ID)


        reactorSocket.connect(EVENT_ID_NEW)


        verify(webSocket, times(1)).send("leaveEvent;$EVENT_ID")
    }


    @Test
    fun `received updateEvent, should call onEventUpdate of callback`() {
        whenever(webSocket.send(any<String>())).then {
            mainWebSocketListener.onMessage(webSocket, "eventUpdate;EVENT_ID;UPDATE_ID")
            true
        }


        reactorSocket.connect(EVENT_ID)


        verify(reactorCallback).onEventUpdate("EVENT_ID", "UPDATE_ID")
    }


    @Test
    fun `received eventTotal, should call onCounterUpdate of callback`() {
        reactorSocket.connect(EVENT_ID)


        mainWebSocketListener.onMessage(webSocket, "eventTotal;ck2343whlc43k0g90i92grc0u;17")


        verify(reactorCallback).onCounterUpdate("17")
    }


    companion object {
        const val EVENT_ID = "ck2343whlc43k0g90i92grc0u"
        const val EVENT_ID_NEW = "kjlhuwhlcjui90i92gretyu"
    }
}