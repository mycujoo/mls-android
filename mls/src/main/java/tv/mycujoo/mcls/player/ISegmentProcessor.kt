package tv.mycujoo.mcls.player

import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
import java.util.concurrent.CopyOnWriteArrayList

interface ISegmentProcessor {

    fun process(segments: List<HlsMediaPlaylist.Segment>)
    fun getDiscontinuityBoundaries(): CopyOnWriteArrayList<Pair<Long, Long>>
    fun getWindowStartTime(): Long
    fun clear()
}
