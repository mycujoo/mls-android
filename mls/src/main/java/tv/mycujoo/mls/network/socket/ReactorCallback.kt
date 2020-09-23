package tv.mycujoo.mls.network.socket

interface ReactorCallback {
    fun onEventUpdate(eventId: String, updateId: String)
    fun onCounterUpdate(counts: String)
    fun onTimelineUpdate(timelineId: String, updateId: String)
}