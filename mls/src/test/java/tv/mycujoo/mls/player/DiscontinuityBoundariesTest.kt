package tv.mycujoo.mls.player

import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DiscontinuityBoundariesTest {

    private lateinit var discontinuityBoundaries: DiscontinuityBoundaries

    var contentSegment = HlsMediaPlaylist.Segment(
        "https://dc9jagk60w3y3mt6171f-0428f4.p5cdn.com/amir/ckhkdu7u801zw010167b2moe5/1080p/1080_segment_1605693445_00000.ts",
        0L,
        0L,
        null,
        null
    )

    var discontinuitySegment =
        HlsMediaPlaylist.Segment(
            "https://dc9jagk60w3y3mt6171f-0428f4.p5cdn.com/amir/ckhkdu7u801zw010167b2moe5/1080p/1080_segment_1605693445_00000.ts",
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

    var discontinuitySegmentWithZeroDuration =
        HlsMediaPlaylist.Segment(
            "https://dc9jagk60w3y3mt6171f-0428f4.p5cdn.com/amir/ckhkdu7u801zw010167b2moe5/1080p/1080_segment_1605693445_00000.ts",
            null,
            "",
            0L,
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

    @Test
    fun `should ignore discontinuity segment with zero duration`() {
        discontinuityBoundaries.segments(listOf(discontinuitySegmentWithZeroDuration))

        assertTrue(discontinuityBoundaries.getBoundaries().isEmpty())
    }


}