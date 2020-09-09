package tv.mycujoo.mls.core

import android.app.Activity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player.STATE_BUFFERING
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SeekParameters
import com.google.android.exoplayer2.ui.TimeBar
import com.npaw.youbora.lib6.plugin.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result.*
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.domain.mapper.TimelineMarkerMapper
import tv.mycujoo.mls.BuildConfig
import tv.mycujoo.mls.analytic.YouboraClient
import tv.mycujoo.mls.api.MLSBuilder
import tv.mycujoo.mls.api.VideoPlayer
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.helper.AnimationFactory
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.helper.ViewersCounterHelper.Companion.isViewersCountValid
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.model.JoinTimelineParam
import tv.mycujoo.mls.network.socket.IReactorSocket
import tv.mycujoo.mls.network.socket.ReactorCallback
import tv.mycujoo.mls.player.IPlayer
import tv.mycujoo.mls.player.Player.Companion.createExoPlayer
import tv.mycujoo.mls.player.Player.Companion.createMediaFactory
import tv.mycujoo.mls.utils.StringUtils
import tv.mycujoo.mls.widgets.MLSPlayerView

class VideoPlayerCoordinator(
    private val videoPlayerConfig: VideoPlayerConfig,
    private val viewHandler: IViewHandler,
    private val reactorSocket: IReactorSocket,
    private val dispatcher: CoroutineScope,
    private val dataManager: IDataManager,
    private val timelineMarkerActionEntities: List<TimelineMarkerEntity>
) {


    /**region Fields*/
    private lateinit var player: IPlayer
    internal lateinit var videoPlayer: VideoPlayer
    private lateinit var playerView: MLSPlayerView

    private var hasAnalytic = false
    private lateinit var youboraClient: YouboraClient
    private var logged = false

    private var isLive = false

    private var eventMayBeStreamed = false

    /**endregion */

    /**region Initialization*/
    fun initialize(MLSPlayerView: MLSPlayerView, player: IPlayer, builder: MLSBuilder) {
        this.playerView = MLSPlayerView
        this.player = player


        reactorSocket.addListener(object : ReactorCallback {
            override fun onEventUpdate(eventId: String, updatedEventId: String) {
                onEventUpdateAvailable(updatedEventId)
            }

            override fun onCounterUpdate(counts: String) {
                if (isLive && isViewersCountValid(counts)) {
                    MLSPlayerView.updateViewersCounter(StringUtils.getNumberOfViewers(counts))
                } else {
                    MLSPlayerView.hideViewersCounter()
                }
            }

            override fun onTimelineUpdate(timelineId: String, timelineUpdateId: String) {
                onTimelineUpdateAvailable(timelineUpdateId)
            }
        })

        player.getDirectInstance()?.let {
            videoPlayer = VideoPlayer(it, this)

            builder.mlsConfiguration.seekTolerance?.let { accuracy ->
                if (accuracy > 0) {
                    it.setSeekParameters(
                        SeekParameters(
                            accuracy / 2,
                            accuracy / 2
                        )
                    )
                }
            }


            builder.playerEventsListener?.let { playerEventsListener ->
                it.addListener(playerEventsListener)
                videoPlayer.playerEventsListener = playerEventsListener
            }
            builder.uiEventListener?.let { uiEventCallback ->
                videoPlayer.uiEventListener = uiEventCallback
                MLSPlayerView.uiEventListener = uiEventCallback

            }

            hasAnalytic = builder.hasAnalytic
            if (builder.hasAnalytic) {
                initAnalytic(builder.internalBuilder, builder.activity!!, it)
            }

            initPlayerViewWrapper(MLSPlayerView, player)
        }
    }

    private fun initPlayerViewWrapper(
        MLSPlayerView: MLSPlayerView,
        player: IPlayer
    ) {
        MLSPlayerView.prepare(
            OverlayViewHelper(viewHandler, AnimationFactory()),
            viewHandler,
            timelineMarkerActionEntities
        )

        val mainEventListener = object : MainEventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)

                handleBufferingProgressBarVisibility(playbackState, playWhenReady)

                handleLiveModeState()

                handlePlayStatusOfOverlayAnimationsWhileBuffering(playbackState, playWhenReady)

                logEventIfNeeded(playbackState)

            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                handlePlayStatusOfOverlayAnimationsOnPlayPause(isPlaying)

            }

        }

        player.addListener(mainEventListener)
        player.loadLastVideo()

        MLSPlayerView.getTimeBar().addListener(object : TimeBar.OnScrubListener {
            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                //do nothing
                MLSPlayerView.scrubbedTo(position)
            }

            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                //do nothing
                MLSPlayerView.scrubStartedAt(position)

            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                MLSPlayerView.scrubStopAt(position)
                timelineMarkerActionEntities.firstOrNull { position in it.offset - 10000L..it.offset + 10000L }
                    ?.let {
                        player.seekTo(it.offset)
                    }
                handleLiveModeState()
            }
        })

        MLSPlayerView.config(videoPlayerConfig)
    }

    fun reInitialize(MLSPlayerView: MLSPlayerView) {
        player.create(createMediaFactory(MLSPlayerView.context), createExoPlayer(MLSPlayerView.context))

        initPlayerViewWrapper(MLSPlayerView, player)
        dataManager.currentEvent?.let {
            joinToReactor(it)
        }
    }

    fun attachPlayer(playerView: MLSPlayerView) {
        playerView.playerView.player = player.getDirectInstance()

        playerView.screenMode(MLSPlayerView.ScreenMode.Portrait(MLSPlayerView.RESIZE_MODE_FILL))

        playerView.playerView.hideController()

        if (hasAnalytic) {
            youboraClient.start()
        }

    }

    private fun initAnalytic(
        internalBuilder: InternalBuilder,
        activity: Activity,
        exoPlayer: ExoPlayer
    ) {
        if (BuildConfig.DEBUG) {
//            YouboraLog.setDebugLevel(YouboraLog.Level.VERBOSE)
        }
        val youboraOptions = Options()
        youboraOptions.accountCode = if (BuildConfig.DEBUG) {
            "mycujoodev"
        } else {
            "mycujoo"
        }
        youboraOptions.isAutoDetectBackground = true

        val plugin = internalBuilder.createYouboraPlugin(youboraOptions, activity)

        plugin.activity = activity
        plugin.adapter = internalBuilder.createExoPlayerAdapter(exoPlayer)

        youboraClient = internalBuilder.createYouboraClient(plugin)
    }

    /**endregion */

    fun onEventUpdateAvailable(updateId: String) {
        dispatcher.launch(context = Dispatchers.Main) {
            val result = dataManager.getEventDetails(updateId)
            when (result) {
                is Success -> {
                    dataManager.currentEvent = result.value
                    if (eventMayBeStreamed.not()) {
                        playVideoOrDisplayEventInfo(result.value)
                    }
                }
                is NetworkError -> {
                }
                is GenericError -> {
                }
            }
        }
    }

    fun onTimelineUpdateAvailable(timelineUpdateId: String) {
        fetchActions(timelineUpdateId)
    }


    /**region Playback functions*/
    fun playVideo(event: EventEntity) {
        playVideo(event.id)
    }

    fun playVideo(eventId: String) {
        isLive = false

        dispatcher.launch(context = Dispatchers.Main) {
            val result = dataManager.getEventDetails(eventId)
            when (result) {
                is Success -> {
                    dataManager.currentEvent = result.value
                    playVideoOrDisplayEventInfo(result.value)
                    fetchActions(result.value)
                    joinToReactor(result.value)
                }
                is NetworkError -> {
                }
                is GenericError -> {
                }
            }
        }
    }

    fun playExternalSourceVideo(videoUri: String) {
        player.play(videoUri)
        playerView.hideEventInfoDialog()
        playerView.hideEventInfoButton()
    }

    private fun playVideoOrDisplayEventInfo(event: EventEntity) {
        playerView.setEventInfo(event.title, event.description, event.start_time)
        playerView.showEventInfoButton()

        if (mayPlayVideo(event)) {
            logged = false

            player.play(event.streams.first().fullUrl)
            playerView.hideEventInfoDialog()
        } else {
            // display event info
            playerView.displayEventInformationPreEventDialog()
        }
    }

    private fun mayPlayVideo(event: EventEntity): Boolean {
        eventMayBeStreamed = event.streams.firstOrNull()?.fullUrl != null
        return eventMayBeStreamed
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
                is Success -> {
                    val timelineMarkerEntityList =
                        result.value.data.mapNotNull { TimelineMarkerMapper.mapToTimelineMarker(it) }
                    playerView.setTimelineMarker(timelineMarkerEntityList)

                    val joinTimelineParam = JoinTimelineParam(timelineId, result.value.data.lastOrNull()?.id)
                    reactorSocket.joinTimelineIfNeeded(joinTimelineParam)
                }
                is NetworkError,
                is GenericError -> {
                }
            }
        }

    }

    /**endregion */

    private fun handleLiveModeState() {

        if (player.isLive()) {
            isLive = true
            if (player.currentPosition() + 15000L >= player.duration()) {
                playerView.setLiveMode(MLSPlayerView.LiveState.LIVE_ON_THE_EDGE)
            } else {
                playerView.setLiveMode(MLSPlayerView.LiveState.LIVE_TRAILING)
            }
        } else {
            // VOD
            isLive = false
            playerView.setLiveMode(MLSPlayerView.LiveState.VOD)
        }

    }

    private fun handlePlayStatusOfOverlayAnimationsOnPlayPause(isPlaying: Boolean) {
        if (isPlaying) {
            playerView.continueOverlayAnimations()
        } else {
            playerView.freezeOverlayAnimations()
        }
    }

    private fun handlePlayStatusOfOverlayAnimationsWhileBuffering(
        playbackState: Int,
        playWhenReady: Boolean
    ) {
        if (playbackState == STATE_BUFFERING && playWhenReady) {
            playerView.freezeOverlayAnimations()

        } else if (playbackState == STATE_READY && playWhenReady) {
            playerView.continueOverlayAnimations()

        }
    }

    private fun handleBufferingProgressBarVisibility(
        playbackState: Int,
        playWhenReady: Boolean
    ) {
        if (playbackState == STATE_BUFFERING && playWhenReady) {
            playerView.showBuffering()
        } else {
            playerView.hideBuffering()
        }
    }

    private fun logEventIfNeeded(playbackState: Int) {
        if (!hasAnalytic) {
            return
        }
        if (logged) {
            return
        }
        if (playbackState == STATE_READY) {
            youboraClient.logEvent(dataManager.currentEvent, player.isLive())
            logged = true
        }
    }


    fun release() {
        player.release()
        if (hasAnalytic) {
            youboraClient.stop()
        }
        reactorSocket.leave(true)
    }

    fun getPlayer(): IPlayer {
        return player
    }


}
