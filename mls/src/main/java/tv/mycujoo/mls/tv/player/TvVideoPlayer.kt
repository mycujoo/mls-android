package tv.mycujoo.mls.tv.player

import android.app.Activity
import android.net.Uri
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.mls.data.IDataManager

class TvVideoPlayer(
    private val activity: Activity,
    videoSupportFragment: VideoSupportFragment,
    private val dispatcher: CoroutineScope,
    private val dataManager: IDataManager
) {

    var player: SimpleExoPlayer? = null
    private var leanbackAdapter: LeanbackPlayerAdapter
    private var glueHost: VideoSupportFragmentGlueHost
    private var mTransportControlGlue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>

    private var eventMayBeStreamed = false


    init {
        player = SimpleExoPlayer.Builder(activity).build()
        leanbackAdapter = LeanbackPlayerAdapter(activity, player!!, 1000)
        glueHost = VideoSupportFragmentGlueHost(videoSupportFragment)

        mTransportControlGlue = PlaybackTransportControlGlue(activity, leanbackAdapter)
        mTransportControlGlue.host = glueHost
        mTransportControlGlue.playWhenPrepared()
    }


    fun playExternalSourceVideo(url: String, isHls: Boolean = true) {
        if (isHls) {
            val userAgent = Util.getUserAgent(activity, "MLS-AndroidTv-SDK")
            val hlsFactory = HlsMediaSource.Factory(DefaultDataSourceFactory(activity, userAgent))

            player!!.prepare(hlsFactory.createMediaSource(Uri.parse(url)))
        } else {
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                activity,
                Util.getUserAgent(activity, "MLS-AndroidTv-SDK")
            )
            val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(url))
            player!!.prepare(videoSource)
        }
    }

    fun playVideo(event: EventEntity) {
        playVideo(event.id)
    }

    fun playVideo(eventId: String) {
        dispatcher.launch(context = Dispatchers.Main) {
            val result = dataManager.getEventDetails(eventId)
            when (result) {
                is Result.Success -> {
                    dataManager.currentEvent = result.value
                    playVideoOrDisplayEventInfo(result.value)
                }
                is Result.NetworkError -> {
                }
                is Result.GenericError -> {
                }
            }
        }
    }

    private fun playVideoOrDisplayEventInfo(event: EventEntity) {

        if (mayPlayVideo(event)) {
            val userAgent = Util.getUserAgent(activity, "MLS-AndroidTv-SDK")
            val hlsFactory = HlsMediaSource.Factory(DefaultDataSourceFactory(activity, userAgent))

            player!!.prepare(hlsFactory.createMediaSource(Uri.parse(event.streams.first().fullUrl)))
        } else {
            // todo displaying event info
        }
    }

    private fun mayPlayVideo(event: EventEntity): Boolean {
        eventMayBeStreamed = event.streams.firstOrNull()?.fullUrl != null
        return eventMayBeStreamed
    }

}
