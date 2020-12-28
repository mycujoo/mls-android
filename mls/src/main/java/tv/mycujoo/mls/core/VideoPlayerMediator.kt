package tv.mycujoo.mls.core

import android.app.Activity
import android.os.Handler
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player.STATE_BUFFERING
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SeekParameters
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.framework.CastSession
import com.npaw.youbora.lib6.plugin.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result.*
import tv.mycujoo.domain.entity.Stream
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.mls.BuildConfig
import tv.mycujoo.mls.analytic.YouboraClient
import tv.mycujoo.mls.api.MLSBuilder
import tv.mycujoo.mls.api.VideoPlayer
import tv.mycujoo.mls.caster.ICaster
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.enum.C
import tv.mycujoo.mls.enum.MessageLevel
import tv.mycujoo.mls.helper.CustomDataBuilder
import tv.mycujoo.mls.helper.MediaInfoBuilder
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.helper.ViewersCounterHelper.Companion.isViewersCountValid
import tv.mycujoo.mls.manager.Logger
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.mediator.AnnotationMediator
import tv.mycujoo.mls.model.JoinTimelineParam
import tv.mycujoo.mls.network.socket.IReactorSocket
import tv.mycujoo.mls.player.*
import tv.mycujoo.mls.player.PlaybackLocation.LOCAL
import tv.mycujoo.mls.player.PlaybackLocation.REMOTE
import tv.mycujoo.mls.player.Player.Companion.createMediaFactory
import tv.mycujoo.mls.utils.StringUtils
import tv.mycujoo.mls.widgets.MLSPlayerView
import tv.mycujoo.mls.widgets.MLSPlayerView.LiveState.LIVE_ON_THE_EDGE
import tv.mycujoo.mls.widgets.MLSPlayerView.LiveState.VOD
import tv.mycujoo.mls.widgets.PlayerControllerMode
import tv.mycujoo.mls.widgets.RemotePlayerControllerListener


class VideoPlayerMediator(
    private var videoPlayerConfig: VideoPlayerConfig,
    private val viewHandler: IViewHandler,
    private val reactorSocket: IReactorSocket,
    private val dispatcher: CoroutineScope,
    private val dataManager: IDataManager,
    private val timelineMarkerActionEntities: List<TimelineMarkerEntity>,
    private val caster: ICaster?,
    logger: Logger
) : AbstractPlayerMediator(reactorSocket, dispatcher, logger) {


    /**region Fields*/
    private lateinit var player: IPlayer
    internal lateinit var videoPlayer: VideoPlayer
    private lateinit var playerView: MLSPlayerView
    private lateinit var annotationMediator: AnnotationMediator

    private var hasAnalytic = false
    private lateinit var youboraClient: YouboraClient
    private var logged = false

    private var joined: Boolean = false
    private var updateId: String? = null
    private lateinit var streamUrlPullJob: Job


    private var playbackState: PlaybackState = PlaybackState.IDLE
    private var playbackLocation: PlaybackLocation = LOCAL
    private var publicKey: String = ""
    private var uuid: String? = ""

    /**endregion */

    /**region Initialization*/
    fun initialize(MLSPlayerView: MLSPlayerView, player: IPlayer, builder: MLSBuilder) {
        this.playerView = MLSPlayerView
        this.player = player
        publicKey = builder.publicKey
        uuid = builder.internalBuilder.uuid

        player.getDirectInstance()?.let {
            videoPlayer = VideoPlayer(it, this, MLSPlayerView)

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

            initPlayerView(MLSPlayerView, player, builder.internalBuilder.overlayViewHelper)
            initCaster(player, MLSPlayerView)
        }
    }

    fun setAnnotationMediator(annotationMediator: AnnotationMediator) {
        this.annotationMediator = annotationMediator
    }

    private fun initPlayerView(
        MLSPlayerView: MLSPlayerView,
        player: IPlayer,
        overlayViewHelper: OverlayViewHelper
    ) {
        MLSPlayerView.prepare(
            overlayViewHelper,
            viewHandler,
            timelineMarkerActionEntities
        )

        val mainEventListener = object : MainEventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)

                handlePlaybackStatus(playWhenReady, playbackState)
                handleBufferingProgressBarVisibility(playbackState, playWhenReady)
                handleLiveModeState()
                handlePlayStatusOfOverlayAnimationsWhileBuffering(playbackState, playWhenReady)

                logEventIfNeeded(playbackState)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                handlePlaybackStatus(isPlaying)
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

    private fun initCaster(
        player: IPlayer,
        MLSPlayerView: MLSPlayerView
    ) {
        fun addRemotePlayerControllerListener() {
            playerView.getRemotePlayerControllerView().listener =
                object : RemotePlayerControllerListener {
                    override fun onPlay() {
                        caster?.play()
                    }

                    override fun onPause() {
                        caster?.pause()
                    }

                    override fun onSeekTo(newPosition: Long) {
                        val mediaSeekOptions =
                            MediaSeekOptions.Builder().setPosition(newPosition).build()
                        caster?.seek(mediaSeekOptions)
                    }

                    override fun onFastForward(amount: Long) {
                        caster?.fastForward(amount)
                    }

                    override fun onRewind(amount: Long) {
                        caster?.rewind(amount)
                    }
                }
        }

        fun updateRemotePlayerWithLocalPlayerData() {
            playerView.getRemotePlayerControllerView().setPosition(player.currentPosition())
            playerView.getRemotePlayerControllerView().setDuration(player.duration())
        }

        caster?.let {
            fun onApplicationConnected(castSession: CastSession?) {
                if (castSession == null) {
                    return
                }
                updatePlaybackLocation(REMOTE)
                switchControllerMode(REMOTE)
                addRemotePlayerControllerListener()
                updateRemotePlayerWithLocalPlayerData()
                dataManager.currentEvent?.let {
                    loadRemoteMedia(it)
                }
                if (player.isPlaying()) {
                    player.pause()
                }
            }

            fun onApplicationDisconnecting(session: CastSession?) {
                updatePlaybackLocation(LOCAL)
                switchControllerMode(LOCAL)
                session?.remoteMediaClient?.let { remoteMediaClient ->
                    player.seekTo(remoteMediaClient.approximateStreamPosition)
                    if (remoteMediaClient.isPlaying) {
                        player.play()
                    } else {
                        player.pause()
                    }
                }
            }

            fun onApplicationDisconnected(session: CastSession?) {
                updatePlaybackLocation(LOCAL)
                switchControllerMode(LOCAL)
            }

            val castListener = object : tv.mycujoo.mls.caster.ICastListener {
                override fun onPlaybackLocationUpdated(isLocal: Boolean) {
                    if (isLocal) {
                        updatePlaybackLocation(LOCAL)
                    } else {
                        updatePlaybackLocation(REMOTE)
                    }
                }

                override fun onConnected(session: CastSession?) {
                    onApplicationConnected(session)
                }

                override fun onDisconnecting(session: CastSession?) {
                    onApplicationDisconnecting(session)
                }

                override fun onDisconnected(session: CastSession?) {
                    onApplicationDisconnected(session)

                }

                override fun onRemoteProgressUpdate(progressMs: Long, durationMs: Long) {
                    playerView.getRemotePlayerControllerView().setPosition(progressMs)
                    playerView.getRemotePlayerControllerView().setDuration(durationMs)
                }

                override fun onRemotePlayStatusUpdate(isPlaying: Boolean, isBuffering: Boolean) {
                    playerView.getRemotePlayerControllerView().setPlayStatus(isPlaying, isBuffering)
                }

                override fun onRemoteLiveStatusUpdate(isLive: Boolean) {
                    playerView.getRemotePlayerControllerView()
                        .setLiveMode(if (isLive) LIVE_ON_THE_EDGE else VOD)
                }

                override fun onCastStateUpdated(showButton: Boolean) {
                    playerView.setCastButtonVisibility(showButton)
                }
            }

            it.initialize(MLSPlayerView.context, castListener)
        }
    }

    fun reInitialize(MLSPlayerView: MLSPlayerView, builder: MLSBuilder) {
        val exoPlayer = Player.createExoPlayer(MLSPlayerView.context)
        player.create(
            MediaFactory(
                createMediaFactory(MLSPlayerView.context),
                com.google.android.exoplayer2.MediaItem.Builder()
            ),
            exoPlayer,
            Handler(),
            MediaOnLoadCompletedListener(exoPlayer)
        )

        initPlayerView(MLSPlayerView, player, builder.internalBuilder.overlayViewHelper)
        dataManager.currentEvent?.let {
            joinEvent(it)
        }
    }

    fun attachPlayer(playerView: MLSPlayerView) {
        playerView.playerView.player = player.getDirectInstance()
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


    fun onResume() {
        caster?.onResume()
    }

    fun onPause() {
        caster?.onPause()
    }


    fun config(videoPlayerConfig: VideoPlayerConfig) {
        if (this::playerView.isInitialized.not()) {
            return
        }

        this.videoPlayerConfig = videoPlayerConfig
        playerView.config(videoPlayerConfig)
    }

    /**endregion */

    /**region Over-ridden functions*/
    override fun onReactorEventUpdate(eventId: String, updateId: String) {
        this.updateId = updateId
        cancelStreamUrlPulling()
        dispatcher.launch(context = Dispatchers.Main) {
            val result = dataManager.getEventDetails(eventId, updateId)
            when (result) {
                is Success -> {
                    dataManager.currentEvent = result.value
                    if (eventMayBeStreamed.not()) {
                        playVideoOrDisplayEventInfo(result.value)
                        startStreamUrlPullingIfNeeded(result.value)
                    }
                    if (!joined) {
                        fetchActions(result.value, updateId)
                    }
                }
                is NetworkError -> {
                    logger.log(MessageLevel.DEBUG, C.NETWORK_ERROR_MESSAGE.plus("${result.error}"))
                }
                is GenericError -> {
                    logger.log(
                        MessageLevel.DEBUG,
                        C.INTERNAL_ERROR_MESSAGE.plus(" ${result.errorMessage} ${result.errorCode}")
                    )
                }
            }
        }
    }

    override fun onReactorCounterUpdate(counts: String) {
        if (videoPlayerConfig.showLiveViewers && isLive && isViewersCountValid(counts)) {
            playerView.updateViewersCounter(StringUtils.getNumberOfViewers(counts))
        } else {
            playerView.hideViewersCounter()
        }
    }

    override fun onReactorTimelineUpdate(timelineId: String, updateId: String) {
        this.updateId = updateId
        fetchActions(timelineId, updateId, false)
    }

    /**endregion */

    /**region Playback functions*/
    override fun playVideo(event: EventEntity) {
        playVideo(event.id)
        storeEvent(event)
    }

    override fun playVideo(eventId: String) {
        isLive = false

        dispatcher.launch(context = Dispatchers.Main) {
            val result = dataManager.getEventDetails(eventId, updateId)
            when (result) {
                is Success -> {
                    dataManager.currentEvent = result.value
                    playVideoOrDisplayEventInfo(result.value)
                    joinEvent(result.value)
                    fetchActions(result.value, true)
                    startStreamUrlPullingIfNeeded(result.value)
                }
                is NetworkError -> {
                    logger.log(MessageLevel.DEBUG, C.NETWORK_ERROR_MESSAGE.plus("${result.error}"))
                }
                is GenericError -> {
                    logger.log(MessageLevel.DEBUG, C.INTERNAL_ERROR_MESSAGE)
                }
            }
        }
    }

    fun playExternalSourceVideo(videoUri: String) {
        player.play(videoUri, Long.MAX_VALUE, videoPlayerConfig.autoPlay)
        playerView.hideEventInfoDialog()
        if (videoPlayerConfig.showEventInfoButton) {
            playerView.showEventInfoButton()
        } else {
            playerView.hideEventInfoButton()
        }
    }

    private fun playVideoOrDisplayEventInfo(event: EventEntity) {
        playerView.setEventInfo(event.title, event.description, event.start_time)
        playerView.setPosterInfo(event.poster_url)
        if (videoPlayerConfig.showEventInfoButton) {
            playerView.showEventInfoButton()
        } else {
            playerView.hideEventInfoButton()
        }

        if (mayPlayVideo(event)) {
            logged = false
            storeEvent(event)
            play(event.streams.first())
            playerView.hideEventInfoDialog()
            playerView.updateControllerVisibility(isPlaying = true)
        } else {
            // display event info
            playerView.showEventInformationForPreEvent()
            playerView.updateControllerVisibility(isPlaying = false)
        }
    }

    private fun play(stream: Stream) {

        if (stream.widevine?.fullUrl != null && stream.widevine.licenseUrl != null) {
            player.play(
                stream.widevine.fullUrl,
                stream.dvrWindow,
                stream.widevine.licenseUrl,
                videoPlayerConfig.autoPlay
            )
        } else if (stream.fullUrl != null) {
            player.play(stream.fullUrl, stream.dvrWindow, videoPlayerConfig.autoPlay)
        }

    }
    /**endregion */


    /**region Reactor function*/
    private fun fetchActions(event: EventEntity, updateId: String) {
        if (event.timeline_ids.isEmpty()) {
            return
        }
        fetchActions(event.timeline_ids.first(), updateId, true)
    }

    private fun fetchActions(event: EventEntity, joinTimeLine: Boolean) {
        if (event.timeline_ids.isEmpty()) {
            return
        }
        fetchActions(event.timeline_ids.first(), null, joinTimeLine)
    }

    private fun fetchActions(timelineId: String, updateId: String?, joinTimeLine: Boolean) {
        if (this::annotationMediator.isInitialized.not()) {
            return
        }

        annotationMediator.fetchActions(timelineId, updateId) { result ->
            when (result) {
                is Success -> {
                    this.updateId = result.value.updateId
//                    joined = true
                    if (joinTimeLine) {
                        val joinTimelineParam = JoinTimelineParam(timelineId, result.value.updateId)
                        reactorSocket.joinTimeline(joinTimelineParam)
                    }
                }
                is NetworkError -> {
                    logger.log(MessageLevel.DEBUG, C.NETWORK_ERROR_MESSAGE.plus("${result.error}"))
                }
                is GenericError -> {
                    logger.log(
                        MessageLevel.DEBUG,
                        C.INTERNAL_ERROR_MESSAGE.plus(" ${result.errorMessage} ${result.errorCode}")
                    )
                }
            }
        }

    }

    /**endregion */

    private fun handleLiveModeState() {
        if (player.isLive()) {
            isLive = true
            if (player.currentPosition() + 20000L >= player.duration()) {
                playerView.setLiveMode(LIVE_ON_THE_EDGE)
            } else {
                playerView.setLiveMode(MLSPlayerView.LiveState.LIVE_TRAILING)
            }
        } else {
            // VOD
            isLive = false
            playerView.setLiveMode(VOD)
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

    private fun handlePlaybackStatus(playWhenReady: Boolean, playbackState: Int) {
        if (playWhenReady) {
            if (playbackState == 3) {
                this.playbackState = PlaybackState.PLAYING
            }
        } else {
            this.playbackState = PlaybackState.IDLE
        }
    }

    private fun handlePlaybackStatus(isPlaying: Boolean) {
        if (isPlaying) {
            this.playbackState = PlaybackState.PLAYING
        } else {
            this.playbackState = PlaybackState.IDLE
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
        cancelPulling()
        player.release()
        if (hasAnalytic) {
            youboraClient.stop()
        }
        reactorSocket.leave(true)
    }

    fun cancelPulling() {
        cancelStreamUrlPulling()
    }

    fun getPlayer(): IPlayer {
        return player
    }

    /**region Cast*/
    private fun loadRemoteMedia(event: EventEntity) {
        if (event.streams.isEmpty() || event.streams.first().fullUrl == null) {
            return
        }
        val fullUrl = event.streams.first().fullUrl!!
        val widevine = event.streams.first().widevine

        val customData =
            CustomDataBuilder.build(event.id, publicKey, uuid, widevine)
        val mediaInfo =
            MediaInfoBuilder.build(fullUrl, event.title, event.thumbnailUrl, customData)

        val mediaLoadOptions: MediaLoadOptions =
            MediaLoadOptions.Builder().setAutoplay(player.isPlaying())
                .setPlayPosition(player.currentPosition())
                .build()
        caster?.loadRemoteMedia(mediaInfo, mediaLoadOptions)
    }

    private fun updatePlaybackLocation(location: PlaybackLocation) {
        playbackLocation = location
    }

    private fun switchControllerMode(playbackLocation: PlaybackLocation) {
        when (playbackLocation) {
            LOCAL -> {
                playerView.switchMode(PlayerControllerMode.EXO_MODE)
            }
            REMOTE -> {
                playerView.switchMode(PlayerControllerMode.REMOTE_CONTROLLER)
            }
        }
    }


    private fun storeEvent(eventEntity: EventEntity) {
        dataManager.currentEvent = eventEntity
    }

    /**endregion */

}
