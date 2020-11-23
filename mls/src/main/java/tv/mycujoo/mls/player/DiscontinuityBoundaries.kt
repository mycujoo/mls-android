package tv.mycujoo.mls.player

import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
import tv.mycujoo.mls.utils.StringUtils
import java.util.concurrent.CopyOnWriteArrayList

class DiscontinuityBoundaries : IDiscontinuityBoundaries {
    private val list = CopyOnWriteArrayList<Pair<Long, Long>>()
    override fun getBoundaries(): CopyOnWriteArrayList<Pair<Long, Long>> {
        return list
    }

    override fun segments(segments: List<HlsMediaPlaylist.Segment>) {
        if (list.isNotEmpty()) {
            list.clear()
        }
        segments.forEach { segment ->
            if (segment.relativeDiscontinuitySequence > 0) {

                val segmentTimeStamp = StringUtils.getSegmentTimeStamp(segment.url)
                if (segmentTimeStamp.toLong() != -1L &&
                    segment.durationUs != 0L
                ) {
                    var duration = segment.durationUs
                    duration /= 1000000
                    list.add(
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
        list.clear()
    }
}
