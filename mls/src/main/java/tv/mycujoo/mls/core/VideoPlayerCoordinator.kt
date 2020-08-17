package tv.mycujoo.mls.core

import android.app.Activity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player.STATE_BUFFERING
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SeekParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.TimeBar
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2Adapter
import com.npaw.youbora.lib6.plugin.Options
import com.npaw.youbora.lib6.plugin.Plugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result.*
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.domain.params.EventIdPairParam
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.domain.usecase.GetEventDetailUseCase
import tv.mycujoo.mls.BuildConfig
import tv.mycujoo.mls.analytic.YouboraClient
import tv.mycujoo.mls.api.MLSBuilder
import tv.mycujoo.mls.api.VideoPlayer
import tv.mycujoo.mls.data.IDataHolder
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.helper.AnimationFactory
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.helper.ViewersCounterHelper.Companion.isViewersCountValid
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.network.socket.IReactorSocket
import tv.mycujoo.mls.network.socket.ReactorCallback
import tv.mycujoo.mls.player.IPlayer
import tv.mycujoo.mls.player.Player.Companion.createExoPlayer
import tv.mycujoo.mls.player.Player.Companion.createMediaFactory
import tv.mycujoo.mls.utils.StringUtils
import tv.mycujoo.mls.widgets.PlayerViewWrapper

class VideoPlayerCoordinator(
    private val videoPlayerConfig: VideoPlayerConfig,
    private val viewIdentifierManager: ViewIdentifierManager,
    private val reactorSocket: IReactorSocket,
    private val dispatcher: CoroutineScope,
    private val eventsRepository: EventsRepository,
    private val dataHolder: IDataHolder,
    private val timelineMarkerActionEntities: List<TimelineMarkerEntity>
) {

    private var hasAnalytic = false
    private lateinit var playerViewWrapper: PlayerViewWrapper

    /**region Fields*/
    private lateinit var player: IPlayer

    internal lateinit var videoPlayer: VideoPlayer


    private var isLive = false

    private var eventMayBeStreamed = false


    private lateinit var youboraClient: YouboraClient


    /**endregion */

    /**region Initialization*/
    fun initialize(playerViewWrapper: PlayerViewWrapper, player: IPlayer, builder: MLSBuilder) {
        this.playerViewWrapper = playerViewWrapper
        this.player = player


        reactorSocket.addListener(object : ReactorCallback {
            override fun onEventUpdate(eventId: String, updatedEventId: String) {
                onEventUpdateAvailable(updatedEventId)
            }

            override fun onCounterUpdate(counts: String) {
                if (isLive && isViewersCountValid(counts)) {
                    playerViewWrapper.updateViewersCounter(StringUtils.getNumberOfViewers(counts))
                } else {
                    playerViewWrapper.hideViewersCounter()
                }
            }
        })

        player.getDirectInstance()?.let {
            videoPlayer = VideoPlayer(it, this)

            builder.mlsConfiguration.accuracy?.let { accuracy ->
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
                playerViewWrapper.uiEventListener = uiEventCallback

            }

            hasAnalytic = builder.hasAnalytic
            if (builder.hasAnalytic) {
                initAnalytic(builder.internalBuilder.uuid!!, builder.activity!!, it)
            }

            initPlayerViewWrapper(playerViewWrapper, player)
        }
    }

    private fun initPlayerViewWrapper(
        playerViewWrapper: PlayerViewWrapper,
        player: IPlayer
    ) {
        playerViewWrapper.prepare(
            OverlayViewHelper(AnimationFactory()),
            viewIdentifierManager,
            emptyList()
        )

        val mainEventListener = object : MainEventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)

                handleBufferingProgressBarVisibility(playbackState, playWhenReady)

                handleLiveModeState()

                handlePlayStatusOfOverlayAnimationsWhileBuffering(playbackState, playWhenReady)

            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                handlePlayStatusOfOverlayAnimationsOnPlayPause(isPlaying)

            }

        }

        player.addListener(mainEventListener)
        player.loadLastVideo()

        playerViewWrapper.getTimeBar().addListener(object : TimeBar.OnScrubListener {
            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                //do nothing
                playerViewWrapper.scrubbedTo(position)
            }

            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                //do nothing
                playerViewWrapper.scrubStartedAt(position)

            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                playerViewWrapper.scrubStopAt(position)
                timelineMarkerActionEntities.firstOrNull { position in it.offset - 10000L..it.offset + 10000L }
                    ?.let {
                        player.seekTo(it.offset)
                    }
                handleLiveModeState()
            }
        })

        playerViewWrapper.config(videoPlayerConfig)
    }

    fun reInitialize(playerViewWrapper: PlayerViewWrapper) {
        player.create(createMediaFactory(playerViewWrapper.context), createExoPlayer(playerViewWrapper.context))

        initPlayerViewWrapper(playerViewWrapper, player)
        dataHolder.currentEvent?.let {
            joinToReactor(it)
        }
    }

    fun attachPlayer(playerViewWrapper: PlayerViewWrapper) {
        playerViewWrapper.playerView.player = player.getDirectInstance()

        playerViewWrapper.screenMode(PlayerViewWrapper.ScreenMode.Portrait(PlayerViewWrapper.RESIZE_MODE_FILL))

        playerViewWrapper.playerView.hideController()

        if (hasAnalytic) {
            youboraClient.start()
        }

    }

    private fun initAnalytic(
        uuid: String,
        activity: Activity,
        exoPlayer: ExoPlayer
    ) {
        if (BuildConfig.DEBUG) {
//            YouboraLog.setDebugLevel(YouboraLog.Level.VERBOSE)
        }
        val youboraOptions = Options()
        //todo : use mls specific Youbora account
        youboraOptions.accountCode = "mycujoodev"
        youboraOptions.isAutoDetectBackground = true

        val plugin = Plugin(youboraOptions, activity)
        plugin.activity = activity
        plugin.adapter = Exoplayer2Adapter(exoPlayer)

        youboraClient = YouboraClient(uuid, plugin)
    }

    /**endregion */

    fun onEventUpdateAvailable(updateId: String) {
        dispatcher.launch(context = Dispatchers.Main) {
            val result = GetEventDetailUseCase(eventsRepository).execute(EventIdPairParam(updateId, updateId))
            when (result) {
                is Success -> {
                    dataHolder.currentEvent = result.value
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

    /**region Playback functions*/
    fun playVideo(event: EventEntity) {
        playVideo(event.id)
    }

    fun playVideo(eventId: String) {
        isLive = false

        dispatcher.launch(context = Dispatchers.Main) {
            val result = GetEventDetailUseCase(eventsRepository).execute(EventIdPairParam(eventId))
            when (result) {
                is Success -> {
                    dataHolder.currentEvent = result.value
                    playVideoOrDisplayEventInfo(result.value)
                    joinToReactor(result.value)
                }
                is NetworkError -> {
                }
                is GenericError -> {
                }
            }
        }
    }

    private fun playVideoOrDisplayEventInfo(event: EventEntity) {

        playerViewWrapper.setEventInfo(event.title, event.description, event.start_time)

        if (mayPlayVideo(event)) {

            player.play(event.streams.first().fullUrl)

            if (hasAnalytic) {
                youboraClient.logEvent(dataHolder.currentEvent)
            }
        } else {
            // display event info
            playerViewWrapper.displayEventInformationPreEventDialog()

        }

    }

    private fun mayPlayVideo(event: EventEntity): Boolean {
        eventMayBeStreamed = event.streams.firstOrNull()?.fullUrl != null
        return eventMayBeStreamed
    }

    /**endregion */


    /**region Reactor function*/

    private fun joinToReactor(event: EventEntity) {
        reactorSocket.join(event.id)
    }

    /**endregion */

    private fun handleLiveModeState() {

        if (player.isLive()) {
            isLive = true
            if (player.currentPosition() + 15000L >= player.duration()) {
                playerViewWrapper.setLiveMode(PlayerViewWrapper.LiveState.LIVE_ON_THE_EDGE)
            } else {
                playerViewWrapper.setLiveMode(PlayerViewWrapper.LiveState.LIVE_TRAILING)
            }
        } else {
            // VOD
            isLive = false
            playerViewWrapper.setLiveMode(PlayerViewWrapper.LiveState.VOD)
        }

    }

    private fun handlePlayStatusOfOverlayAnimationsOnPlayPause(isPlaying: Boolean) {
        if (isPlaying) {
            playerViewWrapper.continueOverlayAnimations()
        } else {
            playerViewWrapper.freezeOverlayAnimations()
        }
    }

    private fun handlePlayStatusOfOverlayAnimationsWhileBuffering(
        playbackState: Int,
        playWhenReady: Boolean
    ) {
        if (playbackState == STATE_BUFFERING && playWhenReady) {
            playerViewWrapper.freezeOverlayAnimations()

        } else if (playbackState == STATE_READY && playWhenReady) {
            playerViewWrapper.continueOverlayAnimations()

        }
    }

    private fun handleBufferingProgressBarVisibility(
        playbackState: Int,
        playWhenReady: Boolean
    ) {
        if (playbackState == STATE_BUFFERING && playWhenReady) {
            playerViewWrapper.showBuffering()
        } else {
            playerViewWrapper.hideBuffering()
        }
    }


    fun release() {
        player.release()
        if (hasAnalytic) {
            youboraClient.stop()
        }
        reactorSocket.leave(true)
    }


    fun getPlayer(): SimpleExoPlayer? {
        return player.getDirectInstance() as SimpleExoPlayer
    }


}
