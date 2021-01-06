package tv.mycujoo.domain.entity

import org.junit.Assert.*
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
}