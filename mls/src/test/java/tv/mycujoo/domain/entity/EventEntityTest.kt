package tv.mycujoo.domain.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tv.mycujoo.data.entity.ServerConstants.Companion.ERROR_CODE_GEOBLOCKED
import tv.mycujoo.data.entity.ServerConstants.Companion.ERROR_CODE_NO_ENTITLEMENT
import tv.mycujoo.data.entity.ServerConstants.Companion.ERROR_CODE_UNSPECIFIED
import tv.mycujoo.mls.enum.StreamStatus

class EventEntityTest {
    @Test
    fun `given event without streams, should return NO_STREAM_URL`() {
        assertEquals(StreamStatus.NO_STREAM_URL, eventWithNoStream().streamStatus())
    }

    @Test
    fun `given event with widevine stream, should return PLAYABLE`() {
        assertEquals(StreamStatus.PLAYABLE, eventWithWidevineVideo().streamStatus())
    }

    @Test
    fun `given stream with raw resource, should return true`() {
        assertTrue(regularStream().isStreamPlayable())
    }

    @Test
    fun `status of event with GeoBlocked stream is GEOBLOCKED`() {
        assertEquals(StreamStatus.GEOBLOCKED, event(listOf(geoBlockedStream())).streamStatus())
    }

    @Test
    fun `status of event with No-Entitlement stream is NO_ENTITLEMENT`() {
        assertEquals(
            StreamStatus.NO_ENTITLEMENT,
            event(listOf(noEntitlementStream())).streamStatus()
        )
    }

    @Test
    fun `status of event with unknown-error stream is UNKNOWN_ERROR`() {
        assertEquals(
            StreamStatus.UNKNOWN_ERROR,
            event(listOf(unknownErrorStream())).streamStatus()
        )
    }


    @Test
    fun `status of event with empty-error stream is UNKNOWN_ERROR`() {
        assertEquals(
            StreamStatus.UNKNOWN_ERROR,
            event(listOf(emptyErrorStream())).streamStatus()
        )
    }

    companion object {
        fun regularStream() = Stream("stream_id", Long.MAX_VALUE.toString(), "sample_url", null)
        fun geoBlockedStream() = Stream(
            id = "stream_id",
            dvrWindowString = Long.MAX_VALUE.toString(),
            fullUrl = null,
            widevine = null,
            error = tv.mycujoo.domain.entity.Error(ERROR_CODE_GEOBLOCKED, null)
        )

        fun noEntitlementStream() = Stream(
            id = "stream_id",
            dvrWindowString = Long.MAX_VALUE.toString(),
            fullUrl = null,
            widevine = null,
            error = tv.mycujoo.domain.entity.Error(ERROR_CODE_NO_ENTITLEMENT, null)
        )

        fun unknownErrorStream() = Stream(
            id = "stream_id",
            dvrWindowString = Long.MAX_VALUE.toString(),
            fullUrl = null,
            widevine = null,
            error = tv.mycujoo.domain.entity.Error(ERROR_CODE_UNSPECIFIED, null)
        )

        fun emptyErrorStream() = Stream(
            id = "stream_id",
            dvrWindowString = Long.MAX_VALUE.toString(),
            fullUrl = null,
            widevine = null,
            error = tv.mycujoo.domain.entity.Error(null, null)
        )

        fun eawVideo(): EventEntity {
            return event(listOf())
        }

        fun eventWithNoStream(): EventEntity {
            return event(emptyList())
        }

        fun eventWithWidevineVideo(): EventEntity {
            val widevine = Widevine("sample_url", "license_url")
            return event(listOf(Stream("stream_id", Long.MAX_VALUE.toString(), null, widevine)))
        }

        private fun event(streams: List<Stream>): EventEntity {
            val location =
                Location(Physical("", "", Coordinates(0.toDouble(), 0.toDouble()), "", ""))
            val event = EventEntity(
                "event_id_0",
                "event_title",
                "event_desc",
                "",
                null,
                location,
                "",
                "",
                EventStatus.EVENT_STATUS_FINISHED,
                streams,
                "",
                emptyList(),
                Metadata(),
                false
            )

            return event
        }
    }

}