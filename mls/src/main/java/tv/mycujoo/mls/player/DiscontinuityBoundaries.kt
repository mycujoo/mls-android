package tv.mycujoo.mls.player

import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
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
                list.add(
                    Pair(
                        segment.relativeStartTimeUs,
                        segment.durationUs
                    )
                )
            }
        }
    }

    override fun clear() {
        list.clear()
    }
}
