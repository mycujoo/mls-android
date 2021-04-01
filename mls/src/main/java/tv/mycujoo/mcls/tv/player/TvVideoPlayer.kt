package tv.mycujoo.mcls.tv.player

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackGlue
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.ads.AdsLoader
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
import tv.mycujoo.domain.entity.Stream
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.core.AbstractPlayerMediator
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.MessageLevel
import tv.mycujoo.mcls.enum.StreamStatus
import tv.mycujoo.mcls.helper.DownloaderClient
import tv.mycujoo.mcls.helper.ViewersCounterHelper.Companion.isViewersCountValid
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.model.JoinTimelineParam
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.player.*
import tv.mycujoo.mcls.player.Player.Companion.createExoPlayer
import tv.mycujoo.mcls.tv.internal.controller.ControllerAgent
import tv.mycujoo.mcls.tv.internal.transport.MLSPlaybackSeekDataProvider
import tv.mycujoo.mcls.tv.internal.transport.MLSPlaybackTransportControlGlueImpl
import tv.mycujoo.mcls.utils.StringUtils
import tv.mycujoo.mcls.widgets.CustomInformationDialog
import tv.mycujoo.mcls.widgets.MLSPlayerView
import tv.mycujoo.mcls.widgets.PreEventInformationDialog
import tv.mycujoo.mcls.widgets.UiEvent
import java.util.concurrent.Executors

@OptIn(ExperimentalStdlibApi::class)
class TvVideoPlayer(
    private val activity: Activity,
    videoSupportFragment: VideoSupportFragment,
    private val ima: IIma?,
    mlsTVConfiguration: MLSTVConfiguration,
    private val mediaFactory: MediaFactory,
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
    private val dialogs = ArrayList<View>()
    private var controllerAgent: ControllerAgent

    private var tvAnnotationMediator: TvAnnotationMediator
    private var overlayContainer: ConstraintLayout
    /**endregion */

    /**region Initializing*/
    init {
        val adViewProvider = addAdViewProvider(videoSupportFragment.view)
        ima?.setAdViewProvider(adViewProvider)

        player = Player()
            .apply {
                val exoplayer = createExoPlayer(activity)
                val mediaOnLoadCompletedListener = MediaOnLoadCompletedListener(exoplayer)
                create(
                    ima,
                    mediaFactory,
                    exoplayer,
                    Handler(),
                    mediaOnLoadCompletedListener
                )
            }
        player.getDirectInstance()?.let { exoPlayer ->
            ima?.let {
                it.setPlayer(exoPlayer)
            }
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

    private fun addAdViewProvider(fragmentView: View?): AdsLoader.AdViewProvider {
        val view = (fragmentView as FrameLayout)
        val frameLayout = FrameLayout(view.context)
        view.addView(frameLayout, 0)
        return AdsLoader.AdViewProvider { frameLayout }
    }

    /**endregion */
    override fun onReactorEventUpdate(eventId: String, updateId: String) {
        cancelStreamUrlPulling()
        dispatcher.launch(context = Dispatchers.Main) {
            val result = dataManager.getEventDetails(eventId, updateId)
            when (result) {
                is Result.Success -> {
                    dataManager.currentEvent = result.value
                    updateStreamStatus(result.value)
                    playVideoOrDisplayEventInfo(result.value)
                    startStreamUrlPullingIfNeeded(result.value)
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

    private fun fetchActions(event: EventEntity, joinTimeLine: Boolean) {
        if (event.timeline_ids.isEmpty()) {
            return
        }
        fetchActions(event.timeline_ids.first(), null, joinTimeLine)
    }

    private fun fetchActions(timelineId: String, updateId: String?, joinTimeline: Boolean) {
        dispatcher.launch(context = Dispatchers.Main) {
            val result = dataManager.getActions(timelineId, updateId)
            when (result) {
                is Result.Success -> {

                    tvAnnotationMediator.feed(result.value.data.map { it.toAction() })

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
                    updateStreamStatus(result.value)
                    playVideoOrDisplayEventInfo(result.value)
                    joinEvent(result.value)
                    fetchActions(result.value, true)
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

        when (streamStatus) {
            StreamStatus.NO_STREAM_URL -> {
                streaming = false
                player.pause()
                displayPreEventInformationLayout()
            }
            StreamStatus.PLAYABLE -> {
                if (streaming.not()) {
                    streaming = true

                    play(event.streams.first())
                    eventInfoContainerLayout.visibility = View.GONE
                    hideInfoDialogs()
                }
            }
            StreamStatus.GEOBLOCKED -> {
                streaming = false
                player.pause()
                showCustomInformationDialog(activity.getString(R.string.message_geoblocked_stream))
            }
            StreamStatus.NO_ENTITLEMENT -> {
                streaming = false
                player.pause()
                showCustomInformationDialog(activity.getString(R.string.message_no_entitlement_stream))
            }
            StreamStatus.UNKNOWN_ERROR -> {
                streaming = false
                player.pause()
                displayPreEventInformationLayout()
            }
        }
    }

    private fun play(stream: Stream) {
        if (stream.widevine?.fullUrl != null && stream.widevine.licenseUrl != null) {
            player.play(
                MediaDatum.DRMMediaData(
                    fullUrl = stream.widevine.fullUrl,
                    dvrWindowSize = stream.getDvrWindowSize(),
                    licenseUrl = stream.widevine.licenseUrl,
                    autoPlay = true
                )
            )
        } else if (stream.fullUrl != null) {
            player.play(
                MediaDatum.MediaData(
                    fullUrl = stream.fullUrl,
                    dvrWindowSize = stream.getDvrWindowSize(),
                    autoPlay = true
                )
            )
        }
    }
    /**endregion */

    /**region Event-information layout*/
    private fun showCustomInformationDialog(message: String) {
        glueHost.hideControlsOverlay(true)
        eventInfoContainerLayout.visibility = View.VISIBLE

        var uiEvent = UiEvent()
        dataManager.currentEvent?.let {
            uiEvent = uiEvent.copy(
                title = it.title,
                description = it.description,
                startTime = it.start_time,
                posterUrl = it.poster_url
            )
        }

        val dialog = CustomInformationDialog(
            container = eventInfoContainerLayout,
            uiEvent = uiEvent,
            message = message
        )
        dialogs.add(dialog)
    }


    private fun displayPreEventInformationLayout() {
        glueHost.hideControlsOverlay(true)
        eventInfoContainerLayout.visibility = View.VISIBLE

        var uiEvent = UiEvent()
        dataManager.currentEvent?.let {
            uiEvent = uiEvent.copy(
                title = it.title,
                description = it.description,
                startTime = it.start_time,
                posterUrl = it.poster_url
            )
        }
        val dialog = PreEventInformationDialog(eventInfoContainerLayout, uiEvent)
        dialogs.add(dialog)
    }

    private fun hideInfoDialogs() {
        dialogs.forEach { dialog ->
            eventInfoContainerLayout.removeView(dialog)
        }
        dialogs.clear()
    }
    /**endregion */
}
