package tv.mycujoo.mls.player

import tv.mycujoo.domain.entity.EventStatus

sealed class MediaDatum(
    open val fullUrl: String,
    open val dvrWindowSize: Long,
    open val eventId: String?,
    open val streamId: String?,
    open val eventStatus: EventStatus?
) {
    data class MediaData(
        override val fullUrl: String,
        override val dvrWindowSize: Long = Long.MAX_VALUE,
        val autoPlay: Boolean,
        override val eventId: String? = null,
        override val streamId: String? = null,
        override val eventStatus: EventStatus? = null
    ) : MediaDatum(
        fullUrl = fullUrl,
        dvrWindowSize = dvrWindowSize,
        eventId = eventId,
        streamId = streamId,
        eventStatus = eventStatus
    )

    data class DRMMediaData(
        override val fullUrl: String,
        override val dvrWindowSize: Long = Long.MAX_VALUE,
        val licenseUrl: String,
        val autoPlay: Boolean,
        override val eventId: String? = null,
        override val streamId: String? = null,
        override val eventStatus: EventStatus? = null
    ) : MediaDatum(
        fullUrl = fullUrl,
        dvrWindowSize = dvrWindowSize,
        eventId = eventId,
        streamId = streamId,
        eventStatus = eventStatus
    )
}
