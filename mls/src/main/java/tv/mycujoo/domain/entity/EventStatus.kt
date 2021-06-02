package tv.mycujoo.domain.entity

import java.util.*

enum class EventStatus {
    EVENT_STATUS_SCHEDULED,
    EVENT_STATUS_RESCHEDULED,
    EVENT_STATUS_CANCELLED,
    EVENT_STATUS_POSTPONED,
    EVENT_STATUS_DELAYED,
    EVENT_STATUS_STARTED,
    EVENT_STATUS_PAUSED,
    EVENT_STATUS_SUSPENDED,
    EVENT_STATUS_FINISHED,
    EVENT_STATUS_UNSPECIFIED;

    companion object {
        fun fromValueOrUnspecified(value: String) =
            values()
                .firstOrNull { it.name.toLowerCase(Locale.ENGLISH) == value }
                ?: EVENT_STATUS_UNSPECIFIED
    }
}