package tv.mycujoo.mls.player

import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SegmentProcessorTest {

    private lateinit var segmentProcessor: SegmentProcessor

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
        segmentProcessor = SegmentProcessor()
    }

    @Test
    fun `empty test`() {
        assertTrue(segmentProcessor.getDiscontinuityBoundaries().isEmpty())
    }

    @Test
    fun `segment with content test`() {
        segmentProcessor.process(listOf(contentSegment))

        assertTrue(segmentProcessor.getDiscontinuityBoundaries().isEmpty())
    }

    @Test
    fun `segment with discontinuity test`() {
        segmentProcessor.process(listOf(discontinuitySegment))

        assertTrue(segmentProcessor.getDiscontinuityBoundaries().isNotEmpty())
    }

    @Test
    fun `should ignore discontinuity segment with zero duration`() {
        segmentProcessor.process(listOf(discontinuitySegmentWithZeroDuration))

        assertTrue(segmentProcessor.getDiscontinuityBoundaries().isEmpty())
    }


}