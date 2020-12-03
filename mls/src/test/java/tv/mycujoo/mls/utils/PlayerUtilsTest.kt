package tv.mycujoo.mls.utils

import org.junit.Assert.assertTrue
import org.junit.Test
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mls.utils.PlayerUtils.Companion.isStreamPlayable
import kotlin.test.assertFalse

class PlayerUtilsTest {
    @Test
    fun `given event with valid raw resource, should return true`() {
        assertTrue(isStreamPlayable(eventWithRawVideo()))
    }

    @Test
    fun `given event without any resource, should return false`() {
        assertFalse(isStreamPlayable(eventWithNoVideo()))
    }

    @Test
    fun `given event with valid widevine resource, should return true`() {
        assertTrue(isStreamPlayable(eventWithWidevineVideo()))
    }


    @Test
    fun `given event with valid widevine and raw resource, should return true`() {
        assertTrue(isStreamPlayable(eventWithBothWidevineAndRawVideo()))
    }


    companion object {
        fun eventWithRawVideo(): EventEntity {
            return event(listOf(Stream("stream_id", Long.MAX_VALUE, "sample_url", null)))
        }

        fun eventWithNoVideo(): EventEntity {
            return event(emptyList())
        }

        fun eventWithWidevineVideo(): EventEntity {
            val widevine = Widevine("sample_url", "license_url")
            return event(listOf(Stream("stream_id", Long.MAX_VALUE, null, widevine)))
        }

        fun eventWithBothWidevineAndRawVideo(): EventEntity {
            val widevine = Widevine("sample_url", "license_url")
            return event(listOf(Stream("stream_id", Long.MAX_VALUE, "license_url", widevine)))
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