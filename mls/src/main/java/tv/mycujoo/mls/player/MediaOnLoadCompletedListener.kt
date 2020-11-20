package tv.mycujoo.mls.player

import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.LoadEventInfo
import com.google.android.exoplayer2.source.MediaLoadData
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.source.hls.HlsManifest
import java.util.concurrent.CopyOnWriteArrayList

class MediaOnLoadCompletedListener(private var exoPlayer: SimpleExoPlayer) :
    MediaSourceEventListener {

    private val discontinuityBoundaries = DiscontinuityBoundaries()
    private var dvrWindowDuration = -1L

    fun getDiscontinuityBoundaries(): CopyOnWriteArrayList<Pair<Long, Long>> {
        return discontinuityBoundaries.getBoundaries()
    }

    fun getDvrWindowDuration(): Long {
        return dvrWindowDuration
    }


    override fun onLoadCompleted(
        windowIndex: Int,
        mediaPeriodId: MediaSource.MediaPeriodId?,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        super.onLoadCompleted(windowIndex, mediaPeriodId, loadEventInfo, mediaLoadData)

        if (exoPlayer.currentTimeline.windowCount > 0) {
            val window = Timeline.Window()
            exoPlayer.currentTimeline.getWindow(0, window)
            if (window.manifest is HlsManifest) {
                // calculate window
                window.windowStartTimeMs
                window.durationMs

                dvrWindowDuration = window.durationMs

                (window.manifest as HlsManifest).mediaPlaylist.segments.let {
                    discontinuityBoundaries.segments(it)

                }
            }
        } else {
            clear()
        }


    }

    fun clear() {
        dvrWindowDuration = -1L
        discontinuityBoundaries.clear()
    }
}