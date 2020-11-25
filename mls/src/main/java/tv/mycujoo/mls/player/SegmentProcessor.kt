package tv.mycujoo.mls.player

import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
import tv.mycujoo.mls.utils.StringUtils
import java.util.concurrent.CopyOnWriteArrayList

class SegmentProcessor : ISegmentProcessor {
    private val dcSegmentsList = CopyOnWriteArrayList<Pair<Long, Long>>()
    private var windowStartTime = -1L

    override fun getDiscontinuityBoundaries(): CopyOnWriteArrayList<Pair<Long, Long>> {
        return dcSegmentsList
    }

    override fun getWindowStartTime(): Long {
        return windowStartTime
    }

    override fun process(segments: List<HlsMediaPlaylist.Segment>) {
        if (dcSegmentsList.isNotEmpty()) {
            dcSegmentsList.clear()
        }
        segments.forEachIndexed { index, segment ->
            if (index == 0) {
                val segmentTimeStamp = StringUtils.getSegmentTimeStamp(segment.url)
                if (segmentTimeStamp.toLong() != -1L) {
                    windowStartTime = segmentTimeStamp.toLong()
                }
            }
            if (segment.relativeDiscontinuitySequence > 0) {

                val segmentTimeStamp = StringUtils.getSegmentTimeStamp(segment.url)
                if (segmentTimeStamp.toLong() != -1L &&
                    segment.durationUs != 0L
                ) {
                    var duration = segment.durationUs
                    duration /= 1000000
                    dcSegmentsList.add(
                        Pair(
                            segmentTimeStamp.toLong(),
                            duration
                        )
                    )
                }
            }
        }
    }

    override fun clear() {
        dcSegmentsList.clear()
    }
}
