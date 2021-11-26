package tv.mycujoo.mcls.ima

import tv.mycujoo.domain.entity.EventStatus
import java.util.*

data class ImaCustomParams(
    val eventId: String? = null,
    val streamId: String? = null,
    val eventStatus: EventStatus? = null
) {
    fun isEmpty(): Boolean {
        return eventId == null && streamId == null && eventStatus == null
    }

    fun writeValues(stringBuilder: StringBuilder, params: Map<String, String>) {
        eventId?.let {
            stringBuilder.append("$AMP_EVENT_ID_EQUALS_TO$eventId")
        }
        streamId?.let { streamId ->
            stringBuilder.append("$AMP_STREAM_ID_EQUALS_TO$streamId")
        }
        eventStatus?.let { eventStatus ->
            val eventStatusLastPart =
                eventStatus.name.lowercase(Locale.ENGLISH).substringAfter(EVENT_STATUS_FIRST_PART)
            stringBuilder.append("$AMP_EVENT_STATUS_EQUALS_TO$eventStatusLastPart")
        }
        params.entries.forEach { entry ->
            stringBuilder.append("&${entry.key}=${entry.value}")
        }
    }

    companion object {
        const val AMP_EVENT_ID_EQUALS_TO = "&eventId="
        const val AMP_STREAM_ID_EQUALS_TO = "&streamId="
        const val AMP_EVENT_STATUS_EQUALS_TO = "&eventStatus="

        const val EVENT_STATUS_FIRST_PART = "event_status_"
    }
}
