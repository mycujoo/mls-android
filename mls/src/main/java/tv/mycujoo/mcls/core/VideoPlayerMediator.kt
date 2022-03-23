package tv.mycujoo.mcls.core

import android.app.Activity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.SeekParameters
import com.google.android.exoplayer2.ui.TimeBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result.*
import tv.mycujoo.domain.entity.Stream
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.analytic.AnalyticsClient
import tv.mycujoo.mcls.analytic.VideoAnalyticsCustomData
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
import tv.mycujoo.mcls.enum.DeviceType
import tv.mycujoo.mcls.enum.MessageLevel
import tv.mycujoo.mcls.enum.StreamStatus.*
import tv.mycujoo.mcls.helper.OverlayViewHelper
import tv.mycujoo.mcls.helper.ViewersCounterHelper.Companion.isViewersCountValid
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.mediator.AnnotationMediator
import tv.mycujoo.mcls.model.JoinTimelineParam
import tv.mycujoo.mcls.network.socket.IBFFRTSocket
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.player.*
import tv.mycujoo.mcls.player.PlaybackLocation.LOCAL
import tv.mycujoo.mcls.player.PlaybackLocation.REMOTE
import tv.mycujoo.mcls.utils.StringUtils
import tv.mycujoo.mcls.utils.ThreadUtils
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import tv.mycujoo.mcls.widgets.MLSPlayerView
import tv.mycujoo.mcls.widgets.MLSPlayerView.LiveState.LIVE_ON_THE_EDGE
import tv.mycujoo.mcls.widgets.MLSPlayerView.LiveState.VOD
import tv.mycujoo.mcls.widgets.PlayerControllerMode
import tv.mycujoo.mcls.widgets.RemotePlayerControllerListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages video-player related components.
 * @param viewHandler handler for add/remove of view (used for Annotations Actions)
 * @param reactorSocket interface for interacting with Reactor service.
 * @param dispatcher coroutine scope context used in I/O bound calls
 * @param dataManager data manager which holds current data(event) loaded
 */
@Singleton
class VideoPlayerMediator @Inject constructor(
    private val viewHandler: IViewHandler,
    private val reactorSocket: IReactorSocket,
    private val dispatcher: CoroutineScope,
    private val dataManager: IDataManager,
    private val logger: Logger,
    private val userPreferencesUtils: UserPreferencesUtils,
    private val player: IPlayer,
    private val overlayViewHelper: OverlayViewHelper,
    private val analyticsClient: AnalyticsClient,
    private val annotationFactory: IAnnotationFactory,
    private val annotationMediator: AnnotationMediator,
    private val bffRtSocket: IBFFRTSocket,
    private val threadUtils: ThreadUtils,
) : AbstractPlayerMediator(reactorSocket, bffRtSocket, dispatcher, logger) {

    private var cast: ICast? = null
    var videoPlayerConfig: VideoPlayerConfig = VideoPlayerConfig.default()

    /**region Fields*/
    var playWhenReadyState = false

    /**
     * SDK exposing video-player
     */
    internal lateinit var videoPlayer: VideoPlayer

    /**
     * MLSPlayerView which exoplayer will integrate with
     */
    private lateinit var playerView: MLSPlayerView

    /**
     * Indicates if SDK user desires to have analytics enabled
     */
    private var hasAnalytic = false

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
     * onConcurrencyLimitExceeded, the extension that the app can use to define it's own behaviour
     * when the limit has been exceeded
     */
    private var onConcurrencyLimitExceeded: (() -> Unit)? = null

    /**
     * Concurrency Limit Feature Toggle
     */
    var concurrencyLimitEnabled = true

    /**
     * Retry action for ConcurrencyRequest
     */
    private var bffSocketRetryDelay = INITIAL_SOCKET_RETRY_DELAY
    private val concurrencyRequestRetryHandler = threadUtils.provideHandler()
    private val concurrencyRequestRetryRunnable = Runnable {
        bffRtSocket.leaveCurrentSession()
        dataManager.currentEvent?.id?.let {
            startWatchSession(it)
        }
    }

    companion object {
        const val INITIAL_SOCKET_RETRY_DELAY = 2000L
    }

    /**endregion */

    /**region Initialization*/
    fun initialize(
        MLSPlayerView: MLSPlayerView,
        builder: MLSBuilder,
        timelineMarkerActionEntities: List<TimelineMarkerEntity> = listOf(),
        cast: ICast? = null
    ) {
        this.playerView = MLSPlayerView
        publicKey = builder.publicKey
        onConcurrencyLimitExceeded = builder.onConcurrencyLimitExceeded
        concurrencyLimitEnabled = builder.concurrencyLimitFeatureEnabled

        player.getDirectInstance()?.let {
            videoPlayer = VideoPlayer(it, this, playerView)

            builder.mlsConfiguration.seekTolerance.let { accuracy ->
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
                playerView.uiEventListener = uiEventCallback

            }

            hasAnalytic = builder.hasAnalytic
            if (builder.hasAnalytic) {
                initAnalytic(
                    builder.activity!!,
                    it,
                    builder.getAnalyticsAccountCode(),
                    builder.customVideoAnalyticsData
                )
            }

            initPlayerView(
                playerView,
                timelineMarkerActionEntities
            )

            if (cast != null) {
                initCaster(cast, player, playerView)
            }
        }
    }

    private fun initPlayerView(
        MLSPlayerView: MLSPlayerView,
        timelineMarkerActionEntities: List<TimelineMarkerEntity>
    ) {
        playerView = MLSPlayerView

        playerView.prepare(
            overlayViewHelper,
            viewHandler,
            timelineMarkerActionEntities
        )

        val mainEventListener = object : MainEventListener {
            /**
             * To Fix the deprecation of onPlayerStateChanged, I replaced it with these 2 functions.
             * onPlayWhenReadyChanged handles the first change of onPlayerStateChanged. which is
             * PlayWhenReady.
             * The Second handles the playback state. This is becuase onPlayWhenReadyChanged emits
             * only 1 of 2 functions. Idle, and NotReady
             */


            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, playbackState)

                playWhenReadyState = playWhenReady
                handlePlaybackStatus(playWhenReady, playbackState)
                handleBufferingProgressBarVisibility(playbackState, playWhenReady)
                handleLiveModeState()
                handlePlayStatusOfOverlayAnimationsWhileBuffering(playbackState, playWhenReady)

                Timber.d("Playback State")
                if (playbackState == STATE_READY) {
                    dataManager.currentEvent?.let { event ->
                        if (event.is_protected && event.isNativeMLS && concurrencyLimitEnabled) {
                            startWatchSession(eventId = event.id)
                        }
                    }
                }


                logEventIfNeeded(playbackState)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                handlePlaybackStatus(playWhenReadyState, playbackState)
                handleBufferingProgressBarVisibility(playbackState, playWhenReadyState)
                handleLiveModeState()
                handlePlayStatusOfOverlayAnimationsWhileBuffering(playbackState, playWhenReadyState)

                logEventIfNeeded(playbackState)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                streaming = isPlaying
                handlePlaybackStatus(isPlaying)
                handlePlayStatusOfOverlayAnimationsOnPlayPause(isPlaying)
            }

        }

        player.addListener(mainEventListener)
        player.loadLastVideo()
        dataManager.currentEvent?.let {
            playerView.setEventInfo(it.title, it.description, it.getFormattedStartTimeDate())
            if (it.poster_url != null) {
                playerView.setPosterInfo(it.poster_url)
            }
        }

        playerView.getTimeBar().addListener(object : TimeBar.OnScrubListener {
            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                //do nothing
                playerView.scrubbedTo(position)
            }

            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                //do nothing
                playerView.scrubStartedAt(position)
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                playerView.scrubStopAt(position)
                timelineMarkerActionEntities.firstOrNull { position in it.offset - 10000L..it.offset + 10000L }
                    ?.let {
                        player.seekTo(it.offset)
                    }
                handleLiveModeState()
            }
        })

        playerView.config(videoPlayerConfig)
    }

    private fun initCaster(
        cast: ICast,
        player: IPlayer,
        MLSPlayerView: MLSPlayerView
    ) {
        this.cast = cast
        this.playerView = MLSPlayerView
        fun addRemotePlayerControllerListener() {
            playerView.getRemotePlayerControllerView().listener =
                object : RemotePlayerControllerListener {
                    override fun onPlay() {
                        cast.play()
                    }

                    override fun onPause() {
                        cast.pause()
                    }

                    override fun onSeekTo(newPosition: Long) {
                        cast.seekTo(newPosition)
                    }

                    override fun onFastForward(amount: Long) {
                        cast.fastForward(amount)
                    }

                    override fun onRewind(amount: Long) {
                        cast.rewind(amount)
                    }
                }
        }

        fun updateRemotePlayerWithLocalPlayerData() {
            playerView.getRemotePlayerControllerView().setPosition(player.currentPosition())
            playerView.getRemotePlayerControllerView().setDuration(player.duration())
        }


        cast.let {
            fun onApplicationDisconnected(casterSession: ICasterSession?) {
                updatePlaybackLocation(LOCAL)
                switchControllerMode(LOCAL)
                player.setIsCasting(false)
                playerView.setIsCasting(false)
                startYoubora()
                casterSession?.getRemoteMediaClient()?.let { remoteMediaClient ->
                    dataManager.currentEvent?.let { event ->
                        Timber.d("Trying to continue ${event.id}")
                        streaming = false
                        playVideoOrDisplayEventInfo(event)
                        player.seekToWhenReady(remoteMediaClient.approximateStreamPosition())
                    }

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
                player.setIsCasting(true)
                playerView.setIsCasting(true)
                updateRemotePlayerWithLocalPlayerData()
                updatePlaybackLocation(REMOTE)
                switchControllerMode(REMOTE)
                addRemotePlayerControllerListener()
                stopYoubora()
                dataManager.currentEvent?.let { event ->
                    loadRemoteMedia(event, player.currentPosition())
                    player.clearQue()
                }
                if (player.isPlaying()) {
                    player.pause()
                }
            }

            fun onCastSessionResumed(casterSession: ICasterSession?) {
                if (casterSession == null) {
                    return
                }
                player.setIsCasting(true)
                playerView.setIsCasting(true)
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

            try {
                it.initialize(playerView.context, castListener)
            } catch (castInitializationException: RuntimeException) {
                logger.log(MessageLevel.ERROR, castInitializationException.message)
            }
        }
    }

    /**
     * Abstracting Analytics client from Youbora
     * Here we can
     */
    private fun initAnalytic(
        activity: Activity,
        exoPlayer: ExoPlayer,
        analyticsAccountCode: String,
        customData: VideoAnalyticsCustomData?
    ) {
        if (analyticsClient is YouboraClient) {
            analyticsClient.setYouboraPlugin(
                activity,
                exoPlayer,
                analyticsAccountCode,
                DeviceType.ANDROID,
                customData
            )
        }
    }

    fun attachPlayer(mlsPlayerView: MLSPlayerView) {
        playerView = mlsPlayerView

        playerView.playerView.player = player.getDirectInstance()
        playerView.playerView.hideController()

        if (hasAnalytic) {
            analyticsClient.start()
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

    /**
     * Changing Video Analytics Custom Data On Runtime After Building
     */
    fun setVideoAnalyticsCustomData(
        activity: Activity,
        analyticsAccountCode: String,
        customData: VideoAnalyticsCustomData?
    ) {
        if (hasAnalytic && analyticsClient is YouboraClient) {
            player.getDirectInstance()?.let { exoPlayer ->
                analyticsClient.setYouboraPlugin(
                    activity,
                    exoPlayer,
                    analyticsAccountCode,
                    DeviceType.ANDROID,
                    customData
                )
            }
        }
    }

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
            when (val result = dataManager.getEventDetails(eventId, updateId)) {
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
        var shouldPlayWhenReady: Boolean? = null

        if (event.id != dataManager.currentEvent?.id) {
            if (streaming) streaming = false
            shouldPlayWhenReady = true
            player.clearQue()
            // Prepare to switch and leave current channel. If trying to reconnect cancel it
            concurrencyRequestRetryHandler.removeCallbacks(concurrencyRequestRetryRunnable)
            bffRtSocket.leaveCurrentSession()
            annotationFactory.clearOverlays()
        }
        dataManager.currentEvent = event
        updateStreamStatus(event)
        playVideoOrDisplayEventInfo(event, shouldPlayWhenReady)

        // If the event is constructed manually and not a native MLS, it should not be replaced with any other version
        if (event.isNativeMLS) {
            joinEvent(event)
            startStreamUrlPullingIfNeeded(event)
            fetchActions(event, true)
        } else {
            cancelStreamUrlPulling()
        }
    }

    /**
     * call API to receive event details and then start playing video,
     * Or display Pre-event screen, if the event has not started yet.
     */
    override fun playVideo(eventId: String) {
        isLive = false

        dispatcher.launch(context = Dispatchers.Main) {
            when (val result = dataManager.getEventDetails(eventId, updateId)) {
                is Success -> {
                    playVideo(result.value)
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
     * internal use: either play video, or display event info dialog.
     * This is decided based on status of stream url of event,
     * if it is playable, video player will start to stream.
     * @param event the event which is about to stream/display info
     * @see StreamStatus
     */
    private fun playVideoOrDisplayEventInfo(event: EventEntity, playWhenReady: Boolean? = null) {
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
                    playerView.hideInfoDialogs()
                    playerView.updateControllerVisibility(isPlaying = true)
                    // If playback is local, depend on the config, else always load the video but don't play
                    if (playbackLocation == LOCAL) {
                        play(event.streams.first(), playbackLocation != REMOTE)
                    } else if (playbackLocation == REMOTE) {
                        loadRemoteMedia(event, 0, playWhenReady)
                    }
                }
            }
            GEOBLOCKED -> {
                streaming = false
                player.pause()
                playerView.showCustomInformationDialog(playerView.resources.getString(R.string.message_geoblocked_stream))
                playerView.updateControllerVisibility(isPlaying = false)
                if (playbackLocation == REMOTE) {
                    cast?.release()
                }
            }
            NO_ENTITLEMENT -> {
                streaming = false
                player.pause()
                playerView.showCustomInformationDialog(playerView.resources.getString(R.string.message_no_entitlement_stream))
                playerView.updateControllerVisibility(isPlaying = false)
                if (playbackLocation == REMOTE) {
                    cast?.release()
                }
            }
            UNKNOWN_ERROR -> {
                streaming = false
                player.pause()
                playerView.showPreEventInformationDialog()
                playerView.updateControllerVisibility(isPlaying = false)
                if (playbackLocation == REMOTE) {
                    cast?.release()
                }
            }
        }
    }

    /**
     * start playing the given Stream
     * @param stream information needed to play an event. including stream url, encoded type, etc
     * @see Stream
     */
    private fun play(stream: Stream, playWhenReady: Boolean? = null) {
        if (stream.widevine?.fullUrl != null && stream.widevine.licenseUrl != null) {
            player.play(
                MediaDatum.DRMMediaData(
                    fullUrl = stream.widevine.fullUrl,
                    dvrWindowSize = stream.getDvrWindowSize(),
                    licenseUrl = stream.widevine.licenseUrl,
                    autoPlay = playWhenReady ?: videoPlayerConfig.autoPlay
                )
            )
        } else if (stream.fullUrl != null) {
            player.play(
                MediaDatum.MediaData(
                    fullUrl = stream.fullUrl,
                    dvrWindowSize = stream.getDvrWindowSize(),
                    autoPlay = playWhenReady ?: videoPlayerConfig.autoPlay
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

    /**
     * region Concurrency functions
     */

    private fun startWatchSession(eventId: String) {
        bffSocketRetryDelay = INITIAL_SOCKET_RETRY_DELAY
        bffRtSocket.startSession(eventId, userPreferencesUtils.getIdentityToken())
    }

    fun setOnConcurrencyLimitExceeded(action: () -> Unit) {
        onConcurrencyLimitExceeded = action
    }

    /**
     * If concurrency Limit Exceeded, show An Error Message (This would be the device started watching earlier)
     */
    override fun onConcurrencyLimitExceeded() {
        val onLimitExceeded = Runnable {
            streaming = false
            player.clearQue()
            annotationFactory.clearOverlays()
            playerView.showCustomInformationDialog(playerView.resources.getString(R.string.message_concurrency_limit_exceeded))
            playerView.updateControllerVisibility(isPlaying = false)
            if (playbackLocation == REMOTE) {
                cast?.release()
            }
        }
        threadUtils.provideHandler().post(onLimitExceeded)

        onConcurrencyLimitExceeded?.invoke()
    }

    override fun onConcurrencyBadRequest(reason: String) {
        logger.log(MessageLevel.ERROR, reason)
    }

    override fun onConcurrencyServerError() {
        concurrencyRequestRetryHandler.postDelayed(concurrencyRequestRetryRunnable, bffSocketRetryDelay)
        bffSocketRetryDelay *= 2
    }

    /**
     * endregion
     */

    /**region Youbora functions*/
    /**
     * Start Youbora to send analytical info, if only SDK is configured to have analytics enabled
     */
    private fun startYoubora() {
        if (hasAnalytic) {
            analyticsClient.start()
        }
    }

    /**
     * Stop Youbora from send analytical info, this needs to happen if only SDK is configured to have analytics enabled
     */
    private fun stopYoubora() {
        if (hasAnalytic) {
            analyticsClient.stop()
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
            analyticsClient.logEvent(dataManager.currentEvent, player.isLive())
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
        reactorSocket.leave(true)
        bffRtSocket.leaveCurrentSession()
        stopYoubora()
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
    private fun loadRemoteMedia(
        event: EventEntity,
        currentPosition: Long,
        playWhenReady: Boolean? = null
    ) {
        Timber.d("loadRemoteMedia: $event")
        if (event.streamStatus() != PLAYABLE) {
            return
        }

        dataManager.currentEvent = event

        val params = if (event.isNativeMLS) {
            CasterLoadRemoteMediaParams(
                id = event.id,
                publicKey = publicKey,
                pseudoUserId = userPreferencesUtils.getPseudoUserId(),
                title = event.title,
                thumbnailUrl = event.thumbnailUrl ?: "",
                isPlaying = playWhenReady ?: player.isPlaying(),
                currentPosition = currentPosition
            )
        } else {
            CasterLoadRemoteMediaParams(
                id = event.id,
                customPlaylistUrl = event.streams[0].fullUrl,
                title = event.title,
                thumbnailUrl = event.thumbnailUrl ?: "",
                isPlaying = playWhenReady ?: player.isPlaying(),
                currentPosition = player.currentPosition()
            )
        }

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

}
