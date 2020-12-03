package tv.mycujoo.mls.tv.player

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackGlue
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
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
import okhttp3.OkHttpClient
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.mls.R
import tv.mycujoo.mls.api.MLSTVConfiguration
import tv.mycujoo.mls.core.AbstractPlayerMediator
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.enum.C
import tv.mycujoo.mls.enum.MessageLevel
import tv.mycujoo.mls.helper.DateTimeHelper
import tv.mycujoo.mls.helper.DownloaderClient
import tv.mycujoo.mls.helper.ViewersCounterHelper.Companion.isViewersCountValid
import tv.mycujoo.mls.manager.Logger
import tv.mycujoo.mls.model.JoinTimelineParam
import tv.mycujoo.mls.network.socket.IReactorSocket
import tv.mycujoo.mls.player.IPlayer
import tv.mycujoo.mls.player.MediaOnLoadCompletedListener
import tv.mycujoo.mls.player.Player
import tv.mycujoo.mls.player.Player.Companion.createExoPlayer
import tv.mycujoo.mls.player.Player.Companion.createMediaFactory
import tv.mycujoo.mls.tv.internal.controller.ControllerAgent
import tv.mycujoo.mls.tv.internal.transport.MLSPlaybackSeekDataProvider
import tv.mycujoo.mls.tv.internal.transport.MLSPlaybackTransportControlGlueImpl
import tv.mycujoo.mls.utils.StringUtils
import tv.mycujoo.mls.widgets.MLSPlayerView
import java.util.concurrent.Executors

@OptIn(ExperimentalStdlibApi::class)
class TvVideoPlayer(
    private val activity: Activity,
    videoSupportFragment: VideoSupportFragment,
    mlsTVConfiguration: MLSTVConfiguration,
    private val reactorSocket: IReactorSocket,
    private val dispatcher: CoroutineScope,
    private val dataManager: IDataManager,
    okHttpClient: OkHttpClient,
    logger: Logger
) : AbstractPlayerMediator(reactorSocket, dispatcher, logger) {


    /**region Fields*/
    var player: IPlayer
    private var leanbackAdapter: LeanbackPlayerAdapter
    private var glueHost: VideoSupportFragmentGlueHost
    private var mTransportControlGlue: MLSPlaybackTransportControlGlueImpl<LeanbackPlayerAdapter>
    private var eventInfoContainerLayout: FrameLayout
    private var controllerAgent: ControllerAgent

    private var tvAnnotationMediator: TvAnnotationMediator
    private var overlayContainer: ConstraintLayout
    /**endregion */

    /**region Initializing*/
    init {
        player = Player()
            .apply {
                val hlsMediaFactory = createMediaFactory(activity)
                val exoplayer = createExoPlayer(activity)
                val mediaOnLoadCompletedListener = MediaOnLoadCompletedListener(exoplayer)
                create(hlsMediaFactory, exoplayer, mediaOnLoadCompletedListener)
            }
        player.getDirectInstance()!!.playWhenReady = mlsTVConfiguration.videoPlayerConfig.autoPlay
        leanbackAdapter = LeanbackPlayerAdapter(activity, player.getDirectInstance()!!, 1000)
        glueHost = VideoSupportFragmentGlueHost(videoSupportFragment)

        val progressBar = ProgressBar(activity)
        progressBar.indeterminateDrawable.setTint(Color.parseColor(mlsTVConfiguration.videoPlayerConfig.primaryColor))
        val layoutParams = FrameLayout.LayoutParams(120, 120)
        layoutParams.gravity = Gravity.CENTER
        progressBar.visibility = View.GONE
        (videoSupportFragment.requireView() as FrameLayout).addView(progressBar, layoutParams)
        controllerAgent = ControllerAgent(player.getPlayer()!!)
        controllerAgent.setBufferProgressBar(progressBar)
        mTransportControlGlue =
            MLSPlaybackTransportControlGlueImpl(
                activity,
                leanbackAdapter,
                mlsTVConfiguration,
                controllerAgent
            )
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

        player.addListener(object : com.google.android.exoplayer2.Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    mTransportControlGlue.seekProvider?.let {
                        (it as MLSPlaybackSeekDataProvider).setSeekPositions(player.duration())
                    }
                }

                if (player.isLive()) {
                    if (player.currentPosition() + 20000L >= player.duration()) {
                        controllerAgent.setControllerLiveMode(MLSPlayerView.LiveState.LIVE_ON_THE_EDGE)
                    } else {
                        controllerAgent.setControllerLiveMode(MLSPlayerView.LiveState.LIVE_TRAILING)
                    }
                } else {
                    // VOD
                    controllerAgent.setControllerLiveMode(MLSPlayerView.LiveState.VOD)
                }
            }
        })

        if (videoSupportFragment.view == null) {
            throw IllegalArgumentException("Fragment must be supported in a state which has active view!")
        } else {
            val rootView = videoSupportFragment.requireView() as FrameLayout

            overlayContainer = ConstraintLayout(rootView.context)
            rootView.addView(
                overlayContainer,
                2,
                FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            )

            tvAnnotationMediator = TvAnnotationMediator(
                player,
                overlayContainer,
                Executors.newScheduledThreadPool(1),
                Handler(Looper.getMainLooper()),
                dispatcher,
                DownloaderClient(okHttpClient)
            )

            eventInfoContainerLayout = FrameLayout(rootView.context)
            rootView.addView(
                eventInfoContainerLayout,
                1,
                FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            )
        }
    }

    /**endregion */
    override fun onReactorEventUpdate(eventId: String, updateId: String) {
        cancelStreamUrlPulling()
        dispatcher.launch(context = Dispatchers.Main) {
            val result = dataManager.getEventDetails(eventId, updateId)
            when (result) {
                is Result.Success -> {
                    dataManager.currentEvent = result.value
                    if (eventMayBeStreamed.not()) {
                        playVideoOrDisplayEventInfo(result.value)
                        startStreamUrlPullingIfNeeded(result.value)
                    }
                }
                is Result.NetworkError -> {
                    logger.log(MessageLevel.DEBUG, C.NETWORK_ERROR_MESSAGE.plus("${result.error}"))
                }
                is Result.GenericError -> {
                    logger.log(
                        MessageLevel.DEBUG,
                        C.INTERNAL_ERROR_MESSAGE.plus(" ${result.errorMessage} ${result.errorCode}")
                    )
                }
            }
        }
    }

    override fun onReactorCounterUpdate(counts: String) {
        if (isLive && isViewersCountValid(counts)) {
            controllerAgent.setViewerCount(StringUtils.getNumberOfViewers(counts))
        } else {
            controllerAgent.hideViewersCount()
        }
    }

    override fun onReactorTimelineUpdate(timelineId: String, updateId: String) {
        fetchActions(timelineId, updateId, false)
    }

    private fun fetchActions(timelineId: String, updateId: String, joinTimeline: Boolean) {
        dispatcher.launch(context = Dispatchers.Main) {
            val result = dataManager.getActions(timelineId, updateId)
            when (result) {
                is Result.Success -> {

                    tvAnnotationMediator.feed(result.value.data.map { it.toActionObject() })

                    if (joinTimeline) {
                        val joinTimelineParam = JoinTimelineParam(timelineId, result.value.updateId)
                        reactorSocket.joinTimeline(joinTimelineParam)
                    }

                }
                is Result.NetworkError -> {
                    logger.log(MessageLevel.DEBUG, C.NETWORK_ERROR_MESSAGE.plus("${result.error}"))
                }

                is Result.GenericError -> {
                    logger.log(
                        MessageLevel.DEBUG,
                        C.INTERNAL_ERROR_MESSAGE.plus(" ${result.errorMessage} ${result.errorCode}")
                    )
                }
            }
        }
    }

    /**region Playback*/
    fun playExternalSourceVideo(url: String, isHls: Boolean = true) {
        if (isHls) {
            val userAgent = Util.getUserAgent(activity, "MLS-AndroidTv-SDK")
            val hlsFactory =
                HlsMediaSource.Factory(DefaultDataSourceFactory(activity, userAgent))

            player.getDirectInstance()!!.prepare(hlsFactory.createMediaSource(Uri.parse(url)))
        } else {
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                activity,
                Util.getUserAgent(activity, "MLS-AndroidTv-SDK")
            )
            val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(url))
            player.getDirectInstance()!!.prepare(videoSource)
        }
    }

    override fun playVideo(event: EventEntity) {
        playVideo(event.id)
    }

    override fun playVideo(eventId: String) {
        dispatcher.launch(context = Dispatchers.Main) {
            val result = dataManager.getEventDetails(eventId)
            when (result) {
                is Result.Success -> {
                    dataManager.currentEvent = result.value
                    playVideoOrDisplayEventInfo(result.value)
                    joinEvent(result.value)
                    startStreamUrlPullingIfNeeded(result.value)

                }
                is Result.NetworkError -> {
                    logger.log(
                        MessageLevel.DEBUG,
                        C.NETWORK_ERROR_MESSAGE.plus("${result.error}")
                    )
                }
                is Result.GenericError -> {
                    logger.log(
                        MessageLevel.DEBUG,
                        C.INTERNAL_ERROR_MESSAGE.plus(" ${result.errorMessage} ${result.errorCode}")
                    )
                }
            }
        }
    }

    private fun playVideoOrDisplayEventInfo(event: EventEntity) {
        if (mayPlayVideo(event)) {
            val userAgent = Util.getUserAgent(activity, "MLS-AndroidTv-SDK")
            val hlsFactory =
                HlsMediaSource.Factory(DefaultDataSourceFactory(activity, userAgent))

            player.getDirectInstance()!!
                .prepare(hlsFactory.createMediaSource(Uri.parse(event.streams.first().fullUrl)))
            eventInfoContainerLayout.visibility = View.GONE
        } else {
            displayPreEventInformationLayout(
                event.poster_url,
                event.title,
                event.description,
                event.start_time
            )
        }
    }

    /**endregion */

    /**region Event-information layout*/
    private fun displayPreEventInformationLayout(
        posterUrl: String?,
        title: String,
        description: String,
        startTime: String
    ) {
        glueHost.hideControlsOverlay(true)
        eventInfoContainerLayout.visibility = View.VISIBLE

        val informationLayout =
            LayoutInflater.from(eventInfoContainerLayout.context)
                .inflate(
                    R.layout.dialog_event_info_pre_event_layout,
                    eventInfoContainerLayout,
                    false
                )
        eventInfoContainerLayout.addView(informationLayout)


        if (posterUrl != null) {
            val posterImageView =
                informationLayout.findViewById<ImageView>(R.id.eventInfoPreEventDialog_posterView)
            val canvasView =
                informationLayout.findViewById<ConstraintLayout>(R.id.eventInfoPreEventDialog_canvasView)
            Glide.with(posterImageView).load(posterUrl)
                .into(posterImageView)

            posterImageView.visibility = View.VISIBLE
            canvasView.visibility = View.GONE
        } else {
            informationLayout.findViewById<TextView>(R.id.eventInfoPreEventDialog_titleTextView).text =
                title
            informationLayout.findViewById<TextView>(R.id.informationDialog_bodyTextView).text =
                description
            informationLayout.findViewById<TextView>(R.id.informationDialog_dateTimeTextView).text =
                DateTimeHelper.getDateTime(startTime)
        }
    }
    /**endregion */
}
