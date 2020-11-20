package tv.mycujoo.mls.player

import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DiscontinuityBoundariesTest {

    private lateinit var discontinuityBoundaries: DiscontinuityBoundaries

    var contentSegment = HlsMediaPlaylist.Segment("", 0L, 0L, null, null)

    var discontinuitySegment =
        HlsMediaPlaylist.Segment(
            "",
            null,
            "",
            1000000L,
            1,
            11568000000L,
            null,
            null,
            null,
            0L,
            -1L,
            false
        )


    @Before
    fun setUp() {
        discontinuityBoundaries = DiscontinuityBoundaries()
    }

    @Test
    fun `empty test`() {
        assertTrue(discontinuityBoundaries.getBoundaries().isEmpty())
    }

    @Test
    fun `segment with content test`() {
        discontinuityBoundaries.segments(listOf(contentSegment))

        assertTrue(discontinuityBoundaries.getBoundaries().isEmpty())
    }

    @Test
    fun `segment with discontinuity test`() {
        discontinuityBoundaries.segments(listOf(discontinuitySegment))

        assertTrue(discontinuityBoundaries.getBoundaries().isNotEmpty())
    }


}