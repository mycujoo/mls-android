package tv.mycujoo.mcls.network.socket

import okhttp3.WebSocket
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class ReactorListenerTest {

    private lateinit var reactorListener: ReactorListener

    @Mock
    lateinit var reactorCallback: ReactorCallback

    @Mock
    lateinit var webSocket: WebSocket

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        reactorListener = ReactorListener(reactorCallback)
    }

    @Test
    fun `given valid event update message, should call onEventUpdate`() {
        val message = "eventUpdate;EVENT_ID;UPDATE_ID"
        reactorListener.onMessage(webSocket, message)


        verify(reactorCallback).onEventUpdate("EVENT_ID", "UPDATE_ID")
    }

    @Test
    fun `given invalid event update message, should not call onEventUpdate`() {
        val message = "eventUpdate;EVENT_ID" // misses the 2nd updated id
        val message1 = "eventUpdate" // misses the 2nd updated id
        reactorListener.onMessage(webSocket, message)
        reactorListener.onMessage(webSocket, message1)
        reactorListener.onMessage(webSocket, "")


        verify(reactorCallback, never()).onEventUpdate(any(), any())
    }

    @Test
    fun `given valid counter update message, should call onCounterUpdate`() {
        val message = "eventTotal;ck2343whlc43k0g90i92grc0u;17"
        reactorListener.onMessage(webSocket, message)


        verify(reactorCallback).onCounterUpdate("17")
    }

    @Test
    fun `given invalid counter update message, should not call onCounterUpdate`() {
        val message = "eventTotal;ck2343whlc43k0g90i92grc0u" // misses counts
        reactorListener.onMessage(webSocket, "")
        reactorListener.onMessage(webSocket, message)


        verify(reactorCallback, never()).onCounterUpdate(any())
    }

    @Test
    fun `given valid timeline update, should call onTimelineUpdate`() {
        val message = "$TIMELINE_UPDATE;id0;id1"
        reactorListener.onMessage(webSocket, message)

        verify(reactorCallback).onTimelineUpdate("id0", "id1")
    }
}