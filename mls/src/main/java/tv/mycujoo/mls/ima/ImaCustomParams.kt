package tv.mycujoo.mls.ima

import tv.mycujoo.domain.entity.EventStatus

data class ImaCustomParams(
    val eventId: String? = null,
    val streamId: String? = null,
    val eventStatus: EventStatus? = null
) {
    fun isEmpty(): Boolean {
        return eventId == null && streamId == null && eventStatus == null
    }
}
