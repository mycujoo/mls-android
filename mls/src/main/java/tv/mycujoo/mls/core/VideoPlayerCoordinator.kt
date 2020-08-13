package tv.mycujoo.mls.core

import android.app.Activity
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player.STATE_BUFFERING
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SeekParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
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
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.domain.usecase.GetActionsFromJSONUseCase
import tv.mycujoo.domain.usecase.GetEventDetailUseCase
import tv.mycujoo.mls.BuildConfig
import tv.mycujoo.mls.analytic.YouboraClient
import tv.mycujoo.mls.api.MLSBuilder
import tv.mycujoo.mls.api.VideoPlayer
import tv.mycujoo.mls.data.IDataHolder
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.helper.AnimationFactory
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.network.socket.IReactorSocket
import tv.mycujoo.mls.network.socket.ReactorCallback
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
    private var exoPlayer: SimpleExoPlayer? = null
    private lateinit var mediaFactory: HlsMediaSource.Factory
    internal lateinit var videoPlayer: VideoPlayer

    private var resumePosition: Long = C.INDEX_UNSET.toLong()
    private var resumeWindow: Int = C.INDEX_UNSET

    private var playWhenReady: Boolean = false
    private var playbackPosition: Long = -1L

    private var isLive = false


    private lateinit var youboraClient: YouboraClient


    /**endregion */

    /**region Initialization*/
    fun initialize(playerViewWrapper: PlayerViewWrapper, builder: MLSBuilder) {
        this.playerViewWrapper = playerViewWrapper


        reactorSocket.addListener(object : ReactorCallback {
            override fun onEventUpdate(eventId: String, updatedEventId: String) {

            }

            override fun onCounterUpdate(counts: String) {
                if (isLive) {
                    playerViewWrapper.updateViewersCounter(StringUtils.getNumberOfViewers(counts))
                } else {
                    playerViewWrapper.hideViewersCounter()
                }
            }
        })

        if (exoPlayer == null) {

            mediaFactory = builder.createMediaFactory(playerViewWrapper.context)
            exoPlayer = builder.createExoPlayer(playerViewWrapper.context)

            exoPlayer?.let {

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

//                hasDefaultPlayerController = builder.hasDefaultController

                hasAnalytic = builder.hasAnalytic
                if (builder.hasAnalytic) {
                    initAnalytic(builder.publicKey, builder.activity!!, it)
                }
            }

            this.playerViewWrapper = playerViewWrapper
//            playerViewWrapper.onSizeChangedCallback = coordinator.onSizeChangedCallback

            playerViewWrapper.prepare(
                OverlayViewHelper(AnimationFactory()),
                viewIdentifierManager,
                GetActionsFromJSONUseCase.mappedActionCollections().timelineMarkerActionList
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

            exoPlayer?.addListener(mainEventListener)

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
                            exoPlayer?.seekTo(it.offset)
                        }
                    handleLiveModeState()
                }
            })

            playerViewWrapper.config(videoPlayerConfig)


        }

    }

    fun attachPlayer(playerViewWrapper: PlayerViewWrapper) {
        playerViewWrapper.playerView.player = exoPlayer
//        playerViewWrapper.defaultController(hasDefaultPlayerController)

        playerViewWrapper.screenMode(PlayerViewWrapper.ScreenMode.PORTRAIT)

        playerViewWrapper.playerView.hideController()

        if (hasAnalytic) {
            youboraClient.start()
        }

    }

    private fun initAnalytic(
        publicKey: String,
        activity: Activity,
        exoPlayer: SimpleExoPlayer
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

        youboraClient = YouboraClient(publicKey, plugin)
    }

    /**endregion */

    /**region Playback functions*/
    fun playVideo(event: EventEntity) {
        playVideo(event.id)
    }

    fun playVideo(eventId: String) {
        isLive = false

        dispatcher.launch(context = Dispatchers.Main) {
            val result = GetEventDetailUseCase(eventsRepository).execute(eventId)
            when (result) {
                is Success -> {
                    playVideoOrDisplayEventInfo(result.value)
                    connectToReactor(result.value)
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
            val mediaSource = mediaFactory.createMediaSource(Uri.parse(event.streams.first().fullUrl))

            if (playbackPosition != -1L) {
                exoPlayer?.seekTo(playbackPosition)
            }

            val haveResumePosition = resumeWindow != C.INDEX_UNSET
            if (haveResumePosition) {
                exoPlayer?.seekTo(resumeWindow, resumePosition)
            }

            exoPlayer?.prepare(mediaSource)
            exoPlayer?.playWhenReady = playWhenReady or this.playWhenReady
            exoPlayer?.playWhenReady = true // todo remove this!


            if (hasAnalytic) {
                youboraClient.logEvent(dataHolder.getCurrentEvent())
            }
        } else {
            // display event info
            playerViewWrapper.displayEventInformationPreEventDialog()

        }
//        this.uri = uri
//        dataHolder.eventLiveData = (
//                Event(
//                    "101",
//                    Stream(listOf(uri)),
//                    "Sample name",
//                    "Sample location",
//                    "started"
//                )
//                )


    }

    private fun mayPlayVideo(event: EventEntity) = event.streams.firstOrNull()?.fullUrl != null

    /**endregion */


    /**region Reactor function*/

    private fun connectToReactor(event: EventEntity) {
        reactorSocket.connect(event.id)
    }

    /**endregion */

    private fun handleLiveModeState() {
        exoPlayer?.let {
            if (it.isCurrentWindowDynamic) {
                // live stream
                isLive = true
                if (it.currentPosition + 15000L >= it.duration) {
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


    private fun updateResumePosition() {
        exoPlayer?.let {
            resumeWindow = it.currentWindowIndex
            resumePosition = if (it.isCurrentWindowSeekable) Math.max(
                0,
                it.currentPosition
            ) else C.POSITION_UNSET.toLong()
        }

    }

    fun release() {
        exoPlayer?.let {
            updateResumePosition()
            playWhenReady = it.playWhenReady
            playbackPosition = it.currentPosition

            if (hasAnalytic) {
                youboraClient.stop()
            }

            it.release()
            exoPlayer = null
        }

    }


    fun getPlayer(): SimpleExoPlayer? {
        return exoPlayer
    }


}
