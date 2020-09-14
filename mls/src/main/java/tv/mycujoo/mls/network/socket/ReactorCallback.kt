package tv.mycujoo.mls.network.socket

interface ReactorCallback {
    fun onEventUpdate(eventId: String, updatedEventId: String)
    fun onCounterUpdate(counts: String)
    fun onTimelineUpdate(timelineId: String, updatedEventId: String)
}