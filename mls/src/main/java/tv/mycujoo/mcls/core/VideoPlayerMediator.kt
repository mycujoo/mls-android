package tv.mycujoo.mcls.core

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
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result.*
import tv.mycujoo.domain.entity.Stream
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.mcls.BuildConfig
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.analytic.YouboraClient
import tv.mycujoo.mcls.api.MLSBuilder
import tv.mycujoo.mcls.api.VideoPlayer
import tv.mycujoo.mcls.cast.CasterLoadRemoteMediaParams
import tv.mycujoo.mcls.cast.ICast
import tv.mycujoo.mcls.cast.ICastListener
import tv.mycujoo.mcls.cast.ICasterSession
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.MessageLevel
import tv.mycujoo.mcls.enum.StreamStatus.*
import tv.mycujoo.mcls.helper.OverlayViewHelper
import tv.mycujoo.mcls.helper.ViewersCounterHelper.Companion.isViewersCountValid
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.mediator.AnnotationMediator
import tv.mycujoo.mcls.model.JoinTimelineParam
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.player.MediaDatum
import tv.mycujoo.mcls.player.PlaybackLocation
import tv.mycujoo.mcls.player.PlaybackLocation.LOCAL
import tv.mycujoo.mcls.player.PlaybackLocation.REMOTE
import tv.mycujoo.mcls.player.PlaybackState
import tv.mycujoo.mcls.utils.StringUtils
import tv.mycujoo.mcls.widgets.MLSPlayerView
import tv.mycujoo.mcls.widgets.MLSPlayerView.LiveState.LIVE_ON_THE_EDGE
import tv.mycujoo.mcls.widgets.MLSPlayerView.LiveState.VOD
import tv.mycujoo.mcls.widgets.PlayerControllerMode
import tv.mycujoo.mcls.widgets.RemotePlayerControllerListener

/**
 * Manages video-player related components.
 * @param videoPlayerConfig configuration for video-player behaviour and visuals
 * @param viewHandler handler for add/remove of view (used for Annotations Actions)
 * @param reactorSocket interface for interacting with Reactor service.
 * @param dispatcher coroutine scope context used in I/O bound calls
 * @param dataManager data manager which holds current data(event) loaded
 * @param cast optional: Cast module used for Google Cast
 */
class VideoPlayerMediator(
    private var videoPlayerConfig: VideoPlayerConfig,
    private val viewHandler: IViewHandler,
    private val reactorSocket: IReactorSocket,
    private val dispatcher: CoroutineScope,
    private val dataManager: IDataManager,
    private val timelineMarkerActionEntities: List<TimelineMarkerEntity>,
    private val cast: ICast?,
    logger: Logger
) : AbstractPlayerMediator(reactorSocket, dispatcher, logger) {


    /**region Fields*/
    /**
     * MLS video-player
     */
    private lateinit var player: IPlayer

    /**
     * SDK exposing video-player
     */
    internal lateinit var videoPlayer: VideoPlayer

    /**
     * MLSPlayerView which exoplayer will integrate with
     */
    private lateinit var playerView: MLSPlayerView

    /**
     * Annotation Mediator to handle Annotation Actions acts
     */
    private lateinit var annotationMediator: AnnotationMediator

    /**
     * Indicates if SDK user desires to have analytics enabled
     */
    private var hasAnalytic = false

    /**
     * Youbora client instance to log analytics through
     */
    private lateinit var youboraClient: YouboraClient

    /**
     * Indicates if current video session is logged or not, for analytical purposes
     */
    private var logged = false

    /**
     * Indicates if Reactor service is active and joined
     */
    private var joined: Boolean = false

    /**
     * Latest updateId received from Reactor service, or null if not joined at all
     */
    private var updateId: String? = null

    /**
     * Current playback state.
     * Defaults to idle
     * @see PlaybackState
     */
    private var playbackState: PlaybackState = PlaybackState.IDLE

    /**
     * Current playback location, indicating if SDK is 'Casting' or not
     * Defaults to local.
     * @see PlaybackLocation
     */
    private var playbackLocation: PlaybackLocation = LOCAL

    /**
     * Non-nullable MLS public key of client
     * Defaults to user input.
     */
    private var publicKey: String = ""

    /**
     * Unique identifier to identify user. Youbora Client is an example where this is used.
     * Defaults to randomly generated UUID if it's users first launch visit to MLS,
     * otherwise it is restored from shared-pref
     */
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
                videoPlayer.setPlayerEventsListener(playerEventsListener)
            }
            builder.uiEventListener?.let { uiEventCallback ->
                videoPlayer.setUIEventListener(uiEventCallback)
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
        dataManager.currentEvent?.let {
            MLSPlayerView.setEventInfo(it.title, it.description, it.getFormattedStartTimeDate())
            if (it.poster_url != null) {
                MLSPlayerView.setPosterInfo(it.poster_url)
            }
        }

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
                        cast?.play()
                    }

                    override fun onPause() {
                        cast?.pause()
                    }

                    override fun onSeekTo(newPosition: Long) {
                        cast?.seekTo(newPosition)
                    }

                    override fun onFastForward(amount: Long) {
                        cast?.fastForward(amount)
                    }

                    override fun onRewind(amount: Long) {
                        cast?.rewind(amount)
                    }
                }
        }

        fun updateRemotePlayerWithLocalPlayerData() {
            playerView.getRemotePlayerControllerView().setPosition(player.currentPosition())
            playerView.getRemotePlayerControllerView().setDuration(player.duration())
        }


        cast?.let {
            fun onApplicationDisconnected(casterSession: ICasterSession?) {
                updatePlaybackLocation(LOCAL)
                switchControllerMode(LOCAL)
                startYoubora()
                casterSession?.getRemoteMediaClient()?.let { remoteMediaClient ->
                    player.seekTo(remoteMediaClient.approximateStreamPosition())
                    if (remoteMediaClient.isPlaying()) {
                        player.play()
                    } else {
                        player.pause()
                    }
                }
            }

            fun onCastSessionStarted(casterSession: ICasterSession?) {
                if (casterSession == null) {
                    return
                }
                updateRemotePlayerWithLocalPlayerData()
                updatePlaybackLocation(REMOTE)
                switchControllerMode(REMOTE)
                addRemotePlayerControllerListener()
                stopYoubora()
                dataManager.currentEvent?.let {
                    loadRemoteMedia(it)
                }
                if (player.isPlaying()) {
                    player.pause()
                }
            }

            fun onCastSessionResumed(casterSession: ICasterSession?) {
                if (casterSession == null) {
                    return
                }
                stopYoubora()
                updatePlaybackLocation(REMOTE)
                switchControllerMode(REMOTE)
                addRemotePlayerControllerListener()
                if (player.isPlaying()) {
                    player.pause()
                }
            }

            val castListener: ICastListener = object : ICastListener {
                override fun onPlaybackLocationUpdated(isLocal: Boolean) {
                    if (isLocal) {
                        updatePlaybackLocation(LOCAL)
                    } else {
                        updatePlaybackLocation(REMOTE)
                    }
                }

                override fun onSessionStarted(session: ICasterSession?) {
                    onCastSessionStarted(session)
                }

                override fun onSessionStartFailed(session: ICasterSession?) {
                    onApplicationDisconnected(session)
                }

                override fun onSessionResumed(session: ICasterSession?) {
                    onCastSessionResumed(session)
                }

                override fun onSessionResumeFailed(session: ICasterSession?) {
                    onApplicationDisconnected(session)
                }

                override fun onSessionEnding(session: ICasterSession?) {
                    onApplicationDisconnected(session)
                }

                override fun onSessionEnded(session: ICasterSession?) {
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
            }

            it.initialize(MLSPlayerView.context, castListener)
        }
    }

    private fun initAnalytic(
        internalBuilder: InternalBuilder,
        activity: Activity,
        exoPlayer: ExoPlayer
    ) {
        val youboraOptions = Options()
        youboraOptions.accountCode = if (BuildConfig.DEBUG) {
            MYCUJOO_DEV_YOUBORA_ACCOUNT_NAME
        } else {
            MYCUJOO_PRODUCTION_YOUBORA_ACCOUNT_NAME
        }
        youboraOptions.isAutoDetectBackground = true

        val plugin = internalBuilder.createYouboraPlugin(youboraOptions, activity)

        plugin.activity = activity
        plugin.adapter = internalBuilder.createExoPlayerAdapter(exoPlayer)

        youboraClient = internalBuilder.createYouboraClient(plugin)
    }

    fun attachPlayer(playerView: MLSPlayerView) {
        playerView.playerView.player = player.getDirectInstance()
        playerView.playerView.hideController()

        if (hasAnalytic) {
            youboraClient.start()
        }

    }

    /**
     * Must be called onResume of host activity/fragment to resume Cast, if integrated with Cast module
     */
    fun onResume() {
        cast?.onResume()
    }

    /**
     * Must be called onPause of host activity/fragment to pause Cast, if integrated with Cast module
     */
    fun onPause() {
        cast?.onPause()
    }

    /**
     * Config PlayerView appearance and behaviour based on user preferences
     */
    fun config(videoPlayerConfig: VideoPlayerConfig) {
        if (this::playerView.isInitialized.not()) {
            return
        }

        this.videoPlayerConfig = videoPlayerConfig
        playerView.config(videoPlayerConfig)
    }

    /**endregion */

    /**region Over-ridden functions*/
    /**
     * called when there is an update in the joined room from Reactor service
     * @param eventId eventId that this update belongs to
     * @param updateId id of this update
     * Both are used to fetch the Event details and Annotation Actions.
     */
    override fun onReactorEventUpdate(eventId: String, updateId: String) {
        this.updateId = updateId
        cancelStreamUrlPulling()
        dispatcher.launch(context = Dispatchers.Main) {
            val result = dataManager.getEventDetails(eventId, updateId)
            when (result) {
                is Success -> {
                    dataManager.currentEvent = result.value
                    updateStreamStatus(result.value)
                    playVideoOrDisplayEventInfo(result.value)
                    startStreamUrlPullingIfNeeded(result.value)
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

    /**
     * Called where an update to viewer counter is raised from Reactor service
     * @param counts number of viewers watching current event
     * Viewer counter must be updated if only the event is Live, configured to display viewers counts,
     * and finally, the count itself is considered as a valid counter.
     * @see ViewersCounterHelper
     */
    override fun onReactorCounterUpdate(counts: String) {
        if (videoPlayerConfig.showLiveViewers && isLive && isViewersCountValid(counts)) {
            playerView.updateViewersCounter(StringUtils.getNumberOfViewers(counts))
        } else {
            playerView.hideViewersCounter()
        }
    }

    /**
     * Called with update on Timeline
     * @param timelineId timelineId of current Event
     * @param updateId latest updateId of current Event
     * Indicates new Annotation Actions has been added to given Timeline.
     * Need to (re)fetch Annotation Actions when this callback is called
     */
    override fun onReactorTimelineUpdate(timelineId: String, updateId: String) {
        this.updateId = updateId
        fetchActions(timelineId, updateId, false)
    }

    /**endregion */

    /**region Playback functions*/
    /**
     * play video & join Reactor if applicable
     * @param event event which should be played
     * Will call the overloaded method with event's ID, which in turn will call API to receive stream-url.
     * So it does not matter the stream url exist in the given param. Always the response from server will be used.
     */
    override fun playVideo(event: EventEntity) {
        if (event.isNativeMLS) {
            playVideo(event.id)
            storeEvent(event)
        } else {
            playExternalEvent(event)
            dataManager.currentEvent = event
            cancelPulling()
            updateStreamStatus(event)
        }
    }

    /**
     * call API to receive event details and then start playing video,
     * Or display Pre-event screen, if the event has not started yet.
     */
    override fun playVideo(eventId: String) {
        isLive = false

        dispatcher.launch(context = Dispatchers.Main) {
            val result = dataManager.getEventDetails(eventId, updateId)
            when (result) {
                is Success -> {
                    dataManager.currentEvent = result.value
                    updateStreamStatus(result.value)
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

    /**
     * internal use: play event which is NOT native to MLS,
     * in other words, user has provided parameter to make a streamable event.
     * @param event the externally defined event which is about to play
     */
    private fun playExternalEvent(event: EventEntity) {
        player.play(
            MediaDatum.MediaData(
                fullUrl = event.streams.first().fullUrl!!,
                dvrWindowSize = Long.MAX_VALUE,
                autoPlay = videoPlayerConfig.autoPlay
            )
        )
        playerView.updateControllerVisibility(videoPlayerConfig.autoPlay)

        playerView.setEventInfo(
            event.title,
            event.description,
            event.getFormattedStartTimeDate()
        )
        playerView.hideInfoDialogs()
        if (videoPlayerConfig.showEventInfoButton) {
            playerView.showEventInfoButton()
        } else {
            playerView.hideEventInfoButton()
        }
    }

    /**
     * internal use: either play video, or display event info dialog.
     * This is decided based on status of stream url of event,
     * if it is playable, video player will start to stream.
     * @param event the event which is about to stream/display info
     * @see StreamStatus
     */
    private fun playVideoOrDisplayEventInfo(event: EventEntity) {
        playerView.setEventInfo(event.title, event.description, event.getFormattedStartTimeDate())
        playerView.setPosterInfo(event.poster_url)
        if (videoPlayerConfig.showEventInfoButton) {
            playerView.showEventInfoButton()
        } else {
            playerView.hideEventInfoButton()
        }

        when (streamStatus) {
            NO_STREAM_URL -> {
                streaming = false
                player.pause()
                playerView.showPreEventInformationDialog()
                playerView.updateControllerVisibility(isPlaying = false)
            }
            PLAYABLE -> {
                if (streaming.not()) {
                    streaming = true
                    logged = false
                    storeEvent(event)
                    play(event.streams.first())
                    playerView.hideInfoDialogs()
                    playerView.updateControllerVisibility(isPlaying = true)
                }
            }
            GEOBLOCKED -> {
                streaming = false
                player.pause()
                playerView.showCustomInformationDialog(playerView.resources.getString(R.string.message_geoblocked_stream))
                playerView.updateControllerVisibility(isPlaying = false)
            }
            NO_ENTITLEMENT -> {
                streaming = false
                player.pause()
                playerView.showCustomInformationDialog(playerView.resources.getString(R.string.message_no_entitlement_stream))
                playerView.updateControllerVisibility(isPlaying = false)
            }
            UNKNOWN_ERROR -> {
                streaming = false
                player.pause()
                playerView.showPreEventInformationDialog()
                playerView.updateControllerVisibility(isPlaying = false)
            }
        }
    }

    /**
     * start playing the given Stream
     * @param stream information needed to play an event. including stream url, encoded type, etc
     * @see Stream
     */
    private fun play(stream: Stream) {
        if (stream.widevine?.fullUrl != null && stream.widevine.licenseUrl != null) {
            player.play(
                MediaDatum.DRMMediaData(
                    fullUrl = stream.widevine.fullUrl,
                    dvrWindowSize = stream.getDvrWindowSize(),
                    licenseUrl = stream.widevine.licenseUrl,
                    autoPlay = videoPlayerConfig.autoPlay
                )
            )
        } else if (stream.fullUrl != null) {
            player.play(
                MediaDatum.MediaData(
                    fullUrl = stream.fullUrl,
                    dvrWindowSize = stream.getDvrWindowSize(),
                    autoPlay = videoPlayerConfig.autoPlay
                )
            )
        }
    }
    /**endregion */

    /**region Local Actions*/
    /**
     * set local (defined by user, not provided from server) Annotation Action list.
     */
    fun setLocalActions(annotations: List<Action>) {
        annotationMediator.setLocalActions(annotations)
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

    /**region Youbora functions*/
    /**
     * Start Youbora to send analytical info, if only SDK is configured to have analytics enabled
     */
    private fun startYoubora() {
        if (hasAnalytic) {
            youboraClient.start()
        }
    }

    /**
     * Stop Youbora from send analytical info, this needs to happen if only SDK is configured to have analytics enabled
     */
    private fun stopYoubora() {
        if (hasAnalytic) {
            youboraClient.stop()
        }
    }

    /**endregion */

    /**region Internal*/
    /**
     * Display "live" badge on screen based on position of video
     */
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

    /**
     * Manages active & running animations based on play status
     * @param isPlaying video player state: true if playing, false otherwise
     */
    private fun handlePlayStatusOfOverlayAnimationsOnPlayPause(isPlaying: Boolean) {
        if (isPlaying) {
            playerView.continueOverlayAnimations()
        } else {
            playerView.freezeOverlayAnimations()
        }
    }

    /**
     * Manages active & running animations based on playback &
     * autoplay status while exoplayer is still buffering the video
     * @param playbackState video player playback state: exoplayer player state
     * @param playWhenReady should start playing after loading is complete
     * @see PlaybackState
     */
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

    /**
     * Manages buffering circular progress bar based on playback & autoplay status
     * @param playbackState video player playback state: exoplayer player state
     * @param playWhenReady should start playing after loading is complete
     * @see PlaybackState
     */
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

    /**
     * set local current playback state based on exoplayer playback & autoplay status
     * @param playWhenReady should start playing after loading is complete
     * @param playbackState video player playback state: exoplayer player state
     * @see PlaybackState
     */
    private fun handlePlaybackStatus(playWhenReady: Boolean, playbackState: Int) {
        if (playWhenReady) {
            if (playbackState == 3) {
                this.playbackState = PlaybackState.PLAYING
            }
        } else {
            this.playbackState = PlaybackState.IDLE
        }
    }

    /**
     * set local current playback state based on exoplayer playback & autoplay status
     * @param isPlaying should start playing after loading is complete
     */
    private fun handlePlaybackStatus(isPlaying: Boolean) {
        if (isPlaying) {
            this.playbackState = PlaybackState.PLAYING
        } else {
            this.playbackState = PlaybackState.IDLE
        }
    }

    /**
     * Log event through Youbora client.
     * Log should take place if only analytics is enabled in configuration and should only happen once per stream
     */
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

    /**
     * store event in data manager for later use
     * @param eventEntity new event to be stored
     */
    private fun storeEvent(eventEntity: EventEntity) {
        dataManager.currentEvent = eventEntity
    }

    /**endregion */

    /**
     * Release resources & leave Reactor service
     */
    fun release() {
        streaming = false
        cancelPulling()
        player.release()
        if (hasAnalytic) {
            youboraClient.stop()
        }
        reactorSocket.leave(true)
    }

    /**
     * Dispatch destroy event to Player
     * @see IPlayer
     */
    fun destroy() {
        player.destroy()
    }

    /**
     * Cancel pulling of stream url
     */
    fun cancelPulling() {
        cancelStreamUrlPulling()
    }

    /**
     * @return video player
     * @see IPlayer
     */
    fun getPlayer(): IPlayer {
        return player
    }

    /**region Cast*/
    /**
     * Load event to cast using Google Cast
     * @param event to be streamed Event
     * Cast module must be integrated by user and configured
     */
    private fun loadRemoteMedia(event: EventEntity) {
        if (event.streams.isEmpty() || event.streams.first().fullUrl == null) {
            return
        }
        val fullUrl = event.streams.first().fullUrl!!
        val widevine = event.streams.first().widevine


        val params = CasterLoadRemoteMediaParams(
            id = event.id,
            publicKey = publicKey,
            uuid = uuid,
            widevine = widevine,
            fullUrl = fullUrl,
            title = event.title,
            thumbnailUrl = event.thumbnailUrl ?: "",
            isPlaying = player.isPlaying(),
            currentPosition = player.currentPosition()
        )

        cast?.loadRemoteMedia(params)
    }

    /**
     * Set location of playback
     * @param location local vs remote
     */
    private fun updatePlaybackLocation(location: PlaybackLocation) {
        playbackLocation = location
    }

    /**
     * Switch between local & remote player controller
     * @param playbackLocation
     * When user is casting through MLS, another view which is Identical to local controller,
     * will be displayed which is called Remote controller
     */
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

    /**endregion */

    /**region Internal class*/
    companion object {
        const val MYCUJOO_DEV_YOUBORA_ACCOUNT_NAME = "mycujoodev"
        const val MYCUJOO_PRODUCTION_YOUBORA_ACCOUNT_NAME = "mycujoo"
    }
    /**endregion */

}
