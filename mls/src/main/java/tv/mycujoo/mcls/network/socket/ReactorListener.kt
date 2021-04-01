package tv.mycujoo.mcls.network.socket

import okhttp3.WebSocket
import okhttp3.WebSocketListener

class ReactorListener(private val reactorCallback: ReactorCallback) : WebSocketListener() {

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        when (val reactorMessage = parseMessage(text)) {
            is ReactorMessage.EventUpdate -> {
                reactorCallback.onEventUpdate(reactorMessage.eventId, reactorMessage.updatedEventId)
            }
            is ReactorMessage.CounterUpdate -> {
                reactorCallback.onCounterUpdate(reactorMessage.counts)
            }
            is ReactorMessage.TimelineUpdate -> {
                reactorCallback.onTimelineUpdate(reactorMessage.timelineId, reactorMessage.updatedEventId)
            }
            is ReactorMessage.Unsupported -> {
                // do nothing
            }
        }
    }

    private fun parseMessage(text: String): ReactorMessage {
        val split = text.split(";")

        if (split.size >= 3 && split[0] == EVENT_TOTAL) {
            return ReactorMessage.CounterUpdate(split[2])
        }
        if (split.size >= 3 && split[0] == EVENT_UPDATE) {
            return ReactorMessage.EventUpdate(split[1], split[2])
        }
        if (split.size >= 3 && split[0] == TIMELINE_UPDATE) {
            return ReactorMessage.TimelineUpdate(split[1], split[2])
        }

        return ReactorMessage.Unsupported()

    }

    sealed class ReactorMessage {
        data class EventUpdate(val eventId: String, val updatedEventId: String) : ReactorMessage()
        data class CounterUpdate(val counts: String) : ReactorMessage()
        data class TimelineUpdate(val timelineId: String, val updatedEventId: String) : ReactorMessage()
        data class Unsupported(val message: String = "") : ReactorMessage()
    }
}