package tv.mycujoo.mls.network.socket

import com.nhaarman.mockitokotlin2.*
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertFailsWith

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
    fun `given join command before setting UUID, should throw UninitializedPropertyAccessException`() {
        assertFailsWith<UninitializedPropertyAccessException> { reactorSocket.join(EVENT_ID) }
    }

    @Test
    fun `given join command after setting UUID, should join`() {
        reactorSocket.setUUID("SAMPLE_UUID")


        reactorSocket.join(EVENT_ID)


        verify(webSocket).send("$JOIN_EVENT$EVENT_ID")
    }

    @Test
    fun `given leave command without destroy after, should disconnect from webSocket but not destroy client`() {
        reactorSocket.setUUID("SAMPLE_UUID")
        reactorSocket.join(EVENT_ID)


        reactorSocket.leave(false)


        verify(webSocket).send("$LEAVE_EVENT$EVENT_ID")
        verify(webSocket, never()).close(any(), any())
    }

    @Test
    fun `given leave command with destroy after, should disconnect from webSocket & destroy socket client`() {
        reactorSocket.setUUID("SAMPLE_UUID")
        reactorSocket.join(EVENT_ID)


        reactorSocket.leave(true)


        verify(webSocket).send("$LEAVE_EVENT$EVENT_ID")
        verify(webSocket).close(NORMAL_CLOSURE_STATUS_CODE, null)
    }

    @Test
    fun `given leave command when not initialized through connect, should do nothing`() {
        reactorSocket.setUUID("SAMPLE_UUID")
        reactorSocket.leave(false)


        verify(webSocket, never()).send(any<String>())
        verify(webSocket, never()).close(any(), any())
    }

    @Test
    fun `given join command when active connection exist, should leave from active connection`() {
        reactorSocket.setUUID("SAMPLE_UUID")
        reactorSocket.join(EVENT_ID)


        reactorSocket.join(EVENT_ID_NEW)


        verify(webSocket).send("$LEAVE_EVENT$EVENT_ID")
    }

    @Test
    fun `given join command after leave, should not leave again`() {
        reactorSocket.setUUID("SAMPLE_UUID")
        reactorSocket.join(EVENT_ID)
        reactorSocket.leave(false)


        reactorSocket.join(EVENT_ID_NEW)


        verify(webSocket, times(1)).send("$LEAVE_EVENT$EVENT_ID")
    }


    @Test
    fun `received updateEvent, should call onEventUpdate of callback`() {
        mainWebSocketListener.onMessage(webSocket, "eventUpdate;EVENT_ID;UPDATE_ID")


        verify(reactorCallback).onEventUpdate("EVENT_ID", "UPDATE_ID")
    }


    @Test
    fun `received eventTotal, should call onCounterUpdate of callback`() {
        mainWebSocketListener.onMessage(webSocket, "eventTotal;ck2343whlc43k0g90i92grc0u;17")


        verify(reactorCallback).onCounterUpdate("17")
    }

    @Test
    fun `given invalid message, should not call reactor callback`() {
        mainWebSocketListener.onMessage(webSocket, "")
        mainWebSocketListener.onMessage(webSocket, " ")
        mainWebSocketListener.onMessage(webSocket, "1")
        mainWebSocketListener.onMessage(webSocket, "a")

        verify(reactorCallback, never()).onEventUpdate(any(), any())
        verify(reactorCallback, never()).onCounterUpdate(any())
    }

    companion object {
        const val SAMPLE_UUID = "aa-bb-cc-dd-ee"
        const val EVENT_ID = "ck2343whlc43k0g90i92grc0u"
        const val EVENT_ID_NEW = "kjlhuwhlcjui90i92gretyu"
    }
}