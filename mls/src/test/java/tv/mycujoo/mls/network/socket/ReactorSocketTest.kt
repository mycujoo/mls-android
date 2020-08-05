package tv.mycujoo.mls.network.socket

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okio.ByteString
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ReactorSocketTest {

    private lateinit var reactorSocket: ReactorSocket
    private lateinit var reactorListener: ReactorListener

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

        reactorSocket = ReactorSocket(okHttpClient)
        reactorListener = ReactorListener(reactorCallback)
    }

    @Test
    fun `given connect command when initialized, should connect to webSocket`() {
        reactorSocket.initialize(reactorListener)


        reactorSocket.connect(EVENT_ID)


        verify(webSocket).send("joinEvent;$EVENT_ID")
    }

    @Test
    fun `given connect command when not initialized, should not connect to webSocket`() {
        reactorSocket.connect(EVENT_ID)


        verify(webSocket, never()).send(any<String>())
        verify(webSocket, never()).send(any<ByteString>())
    }

//    @Test
//    fun `received updateEvent, should call onEventUpdate of callback`() {
//        whenever(webSocket.send(any<String>())).then {
//            reactorListener.onMessage(webSocket, "eventUpdate;EVENT_ID;UPDATE_ID")
//            true
//        }
//
//
//        reactorSocket.connect(EVENT_ID)
//
//
//        verify(reactorCallback).onEventUpdate("EVENT_ID", "UPDATE_ID")
//    }
//
//
//    @Test
//    fun `received eventTotal, should call onCounterUpdate of callback`() {
//        reactorSocket.connect(EVENT_ID)
//
//        reactorListener.onMessage(webSocket, "eventTotal;ck2343whlc43k0g90i92grc0u;17")
//
//
//        verify(reactorCallback).onCounterUpdate("17")
//    }


    companion object {
        const val EVENT_ID = "ck2343whlc43k0g90i92grc0u"
    }
}