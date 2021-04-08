package tv.mycujoo.mcls.ima

import org.junit.Test
import tv.mycujoo.domain.entity.EventStatus
import tv.mycujoo.mcls.ima.ImaCustomParams.Companion.AMP_EVENT_ID_EQUALS_TO
import tv.mycujoo.mcls.ima.ImaCustomParams.Companion.AMP_EVENT_STATUS_EQUALS_TO
import tv.mycujoo.mcls.ima.ImaCustomParams.Companion.AMP_STREAM_ID_EQUALS_TO
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ImaCustomParamsTest {
    @Test
    fun `all-Null-fields ImaCustomParams is considered Empty`() {
        val emptyImaCustomParams =
            ImaCustomParams(eventId = null, streamId = null, eventStatus = null)
        val imaCustomParamsWithEventId =
            ImaCustomParams(eventId = "id_00", streamId = null, eventStatus = null)
        val imaCustomParamsWithStreamId =
            ImaCustomParams(eventId = null, streamId = "id_00", eventStatus = null)
        val imaCustomParamsWithEventStatus =
            ImaCustomParams(
                eventId = null,
                streamId = null,
                eventStatus = EventStatus.EVENT_STATUS_SCHEDULED
            )

        assert(emptyImaCustomParams.isEmpty())
        assertFalse(imaCustomParamsWithEventId.isEmpty())
        assertFalse(imaCustomParamsWithStreamId.isEmpty())
        assertFalse(imaCustomParamsWithEventStatus.isEmpty())
    }

    @Test
    fun `empty-ImaCustomParams does not write any values to string-builder`() {
        val stringBuilder = StringBuilder()
        val eventId = "eventId_00"
        val emptyImaCustomParams = ImaCustomParams()


        emptyImaCustomParams.writeValues(stringBuilder)


        assert(stringBuilder.toString().isEmpty())
    }


    @Test
    fun `imaCustomParams writes all non-null-values to string-builder`() {
        val stringBuilder = StringBuilder()
        val eventId = "eventId_00"
        val streamId = "streamId_00"
        val imaCustomParams = ImaCustomParams(
            eventId = eventId,
            streamId = streamId,
            eventStatus = EventStatus.EVENT_STATUS_SCHEDULED
        )


        imaCustomParams.writeValues(stringBuilder)


        assertEquals(
            "$AMP_EVENT_ID_EQUALS_TO$eventId$AMP_STREAM_ID_EQUALS_TO$streamId${AMP_EVENT_STATUS_EQUALS_TO}scheduled",
            stringBuilder.toString()
        )
    }


}