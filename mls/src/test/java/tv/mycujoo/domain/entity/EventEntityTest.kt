package tv.mycujoo.domain.entity

import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tv.mycujoo.data.entity.ServerConstants.Companion.ERROR_CODE_GEOBLOCKED
import tv.mycujoo.data.entity.ServerConstants.Companion.ERROR_CODE_NO_ENTITLEMENT
import tv.mycujoo.data.entity.ServerConstants.Companion.ERROR_CODE_UNSPECIFIED
import tv.mycujoo.mls.enum.StreamStatus
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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

    @Test
    fun `event with valid date, return valid formattedStartDate`() {
        val formattedStartTimeDate = eventWithStartDate().getFormattedStartTimeDate()
        assertNotNull(formattedStartTimeDate)
    }
    @Test
    fun `event with no date, returns null as formattedStartDate`() {
        assertNull(event(emptyList()).getFormattedStartTimeDate())
    }

    companion object {
        fun regularStream() = Stream("stream_id", Long.MAX_VALUE.toString(), "sample_url", null)
        fun geoBlockedStream() = Stream(
            id = "stream_id",
            dvrWindowString = Long.MAX_VALUE.toString(),
            fullUrl = null,
            widevine = null,
            errorCodeAndMessage = ErrorCodeAndMessage(ERROR_CODE_GEOBLOCKED, null)
        )

        fun noEntitlementStream() = Stream(
            id = "stream_id",
            dvrWindowString = Long.MAX_VALUE.toString(),
            fullUrl = null,
            widevine = null,
            errorCodeAndMessage = ErrorCodeAndMessage(ERROR_CODE_NO_ENTITLEMENT, null)
        )

        fun unknownErrorStream() = Stream(
            id = "stream_id",
            dvrWindowString = Long.MAX_VALUE.toString(),
            fullUrl = null,
            widevine = null,
            errorCodeAndMessage = ErrorCodeAndMessage(ERROR_CODE_UNSPECIFIED, null)
        )

        fun emptyErrorStream() = Stream(
            id = "stream_id",
            dvrWindowString = Long.MAX_VALUE.toString(),
            fullUrl = null,
            widevine = null,
            errorCodeAndMessage = ErrorCodeAndMessage(null, null)
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
                null,
                EventStatus.EVENT_STATUS_FINISHED,
                streams,
                "",
                emptyList(),
                Metadata(),
                false
            )

            return event
        }
        private fun eventWithStartDate(): EventEntity {
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
                DateTime("2020-07-11T07:32:46Z"),
                EventStatus.EVENT_STATUS_FINISHED,
                emptyList(),
                "",
                emptyList(),
                Metadata(),
                false
            )

            return event
        }
    }

}