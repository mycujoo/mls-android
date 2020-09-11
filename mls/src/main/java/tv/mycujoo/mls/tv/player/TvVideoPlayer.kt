package tv.mycujoo.mls.tv.player

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackGlue
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
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
import tv.mycujoo.domain.mapper.TimelineMarkerMapper
import tv.mycujoo.mls.R
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.helper.DateTimeHelper
import tv.mycujoo.mls.model.JoinTimelineParam
import tv.mycujoo.mls.network.socket.IReactorSocket
import tv.mycujoo.mls.tv.internal.transport.MLSPlaybackSeekDataProvider
import tv.mycujoo.mls.tv.internal.transport.MLSPlaybackTransportControlGlueImpl

class TvVideoPlayer(
    private val activity: Activity,
    videoSupportFragment: VideoSupportFragment,
    private val reactorSocket: IReactorSocket,
    private val dispatcher: CoroutineScope,
    private val dataManager: IDataManager
) {
    /**region Fields*/
    var player: SimpleExoPlayer? = null
    private var leanbackAdapter: LeanbackPlayerAdapter
    private var glueHost: VideoSupportFragmentGlueHost
    private var mTransportControlGlue: MLSPlaybackTransportControlGlueImpl<LeanbackPlayerAdapter>
    private var eventInfoContainerLayout: FrameLayout

    private var eventMayBeStreamed = false
    /**endregion */

    /**region Initializing*/
    init {
        player = SimpleExoPlayer.Builder(activity).build()
        leanbackAdapter = LeanbackPlayerAdapter(activity, player!!, 1000)
        glueHost = VideoSupportFragmentGlueHost(videoSupportFragment)

        mTransportControlGlue = MLSPlaybackTransportControlGlueImpl(activity, leanbackAdapter)
        mTransportControlGlue.host = glueHost
        mTransportControlGlue.playWhenPrepared()
        if (mTransportControlGlue.isPrepared) {
            mTransportControlGlue.seekProvider =
                MLSPlaybackSeekDataProvider(5000L)
        } else {
            mTransportControlGlue.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
                override fun onPreparedStateChanged(glue: PlaybackGlue?) {
                    if (glue?.isPrepared == true) {
                        glue.removePlayerCallback(this)
                        val transportControlGlue =
                            glue as MLSPlaybackTransportControlGlueImpl<LeanbackPlayerAdapter>
                        transportControlGlue.seekProvider = MLSPlaybackSeekDataProvider(5000L)
                    }

                }
            })
        }

        player!!.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    mTransportControlGlue.seekProvider?.let {
                        (it as MLSPlaybackSeekDataProvider).setSeekPositions(player!!.duration)
                    }
                }
            }
        })

        if (videoSupportFragment.view == null) {
            throw IllegalArgumentException("Fragment must be supported in a state which has active view!")
        } else {
            val rootView = videoSupportFragment.view!! as FrameLayout
            eventInfoContainerLayout = FrameLayout(rootView.context)
//            rootView.addView(eventInfoContainerLayout, 1, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }
    }
    /**endregion */

    /**region Playback*/

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
//                    playVideoOrDisplayEventInfo(result.value)
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
            displayPreEventInformationDialog(event.title, event.description, event.start_time)
        }
    }

    private fun mayPlayVideo(event: EventEntity): Boolean {
        eventMayBeStreamed = event.streams.firstOrNull()?.fullUrl != null
        return eventMayBeStreamed
    }

    /**endregion */

    /**region Event-information dialog*/
    private fun displayPreEventInformationDialog(
        title: String,
        description: String,
        startTime: String
    ) {
        glueHost.hideControlsOverlay(true)

        val informationDialog =
            LayoutInflater.from(eventInfoContainerLayout.context)
                .inflate(
                    R.layout.dialog_event_info_pre_event_layout,
                    eventInfoContainerLayout,
                    false
                )
        eventInfoContainerLayout.addView(informationDialog)

        informationDialog.findViewById<TextView>(R.id.eventInfoPreEventDialog_titleTextView).text =
            title
        informationDialog.findViewById<TextView>(R.id.informationDialog_bodyTextView).text =
            description
        informationDialog.findViewById<TextView>(R.id.informationDialog_dateTimeTextView).text =
            DateTimeHelper.getDateTime(startTime)
    }
    /**endregion */

    /**region Reactor function*/

    private fun joinToReactor(event: EventEntity) {
        reactorSocket.joinEvent(event.id)
    }

    private fun fetchActions(event: EventEntity) {
        if (event.timeline_ids.isEmpty()) {
            return
        }
        fetchActions(event.timeline_ids.first())
    }

    private fun fetchActions(timelineId: String) {
        dispatcher.launch(context = Dispatchers.Main) {
            val result = dataManager.getActions(timelineId)
            when (result) {
                is Result.Success -> {
                    val timelineMarkerEntityList =
                        result.value.data.mapNotNull { TimelineMarkerMapper.mapToTimelineMarker(it) }
//                    playerView.setTimelineMarker(timelineMarkerEntityList) todo

                    val joinTimelineParam =
                        JoinTimelineParam(timelineId, result.value.data.lastOrNull()?.id)
                    reactorSocket.joinTimelineIfNeeded(joinTimelineParam)
                }
                is Result.NetworkError,
                is Result.GenericError -> {
                }
            }
        }

    }

    /**endregion */

}
