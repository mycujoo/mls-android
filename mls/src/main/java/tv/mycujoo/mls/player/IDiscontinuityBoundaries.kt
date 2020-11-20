package tv.mycujoo.mls.player

import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
import java.util.concurrent.CopyOnWriteArrayList

interface IDiscontinuityBoundaries {

    fun segments(segments: List<HlsMediaPlaylist.Segment>)
    fun getBoundaries(): CopyOnWriteArrayList<Pair<Long, Long>>
    fun clear()
}
