package tv.mycujoo.domain.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class StreamTest {
    @Test
    fun `Stream dvrWindow size test`() {
        val streamWith120000DvrWindow = Stream("id_00", "120000", null, null)
        val streamWith0DvrWindow = Stream("id_00", "0", null, null)
        val streamWithInvalidDvrWindow = Stream("id_00", "", null, null)


        assertEquals(120000L, streamWith120000DvrWindow.getDvrWindowSize())
        assertEquals(0L, streamWith0DvrWindow.getDvrWindowSize())
        assertEquals(Long.MAX_VALUE, streamWithInvalidDvrWindow.getDvrWindowSize())
    }

    @Test
    fun `non-GEOBLOCKED stream should return false for isGeoBlocked`() {
        val stream = Stream("id_0", "120000", null, null)


        assertFalse(stream.isGeoBlocked())
    }

    @Test
    fun `GEOBLOCKED stream should return true for isGeoBlocked`() {
        val geoBlockedStream = Stream(
            id = "id_0", dvrWindowString = "120000", fullUrl = null, widevine = null,
            errorCodeAndMessage = ErrorCodeAndMessage(
                code = "ERROR_CODE_GEOBLOCKED",
                message = "This stream is Geo-blocked"
            )
        )

        assert(geoBlockedStream.isGeoBlocked())
    }

    @Test
    fun `regular stream should return false for isNoEntitlement`() {
        val stream = Stream("id_0", "120000", null, null)


        assertFalse(stream.isNoEntitlement())
    }

    @Test
    fun `no-entitlement stream should return true for isNoEntitlement`() {
        val stream = Stream(
            id = "id_0", dvrWindowString = "120000", fullUrl = null, widevine = null,
            errorCodeAndMessage = ErrorCodeAndMessage(
                code = "ERROR_CODE_NO_ENTITLEMENT",
                message = "Access to this stream is restricted"
            )
        )


        assert(stream.isNoEntitlement())
    }

    @Test
    fun `regular stream should return false for hasUnknownError`() {
        val stream = Stream("id_0", "120000", null, null)


        assertFalse(stream.hasUnknownError())
    }

    @Test
    fun `stream with unknown-error should return true for hasUnknownError`() {
        val streamWithUnknownError = Stream(
            "id_0", "120000", null, null,
            errorCodeAndMessage = ErrorCodeAndMessage(
                code = "ERROR_CODE_UNSPECIFIED",
                message = null
            )
        )


        assert(streamWithUnknownError.hasUnknownError())
    }
}