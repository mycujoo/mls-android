package tv.mycujoo.mls.core

import android.app.Activity
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player.STATE_BUFFERING
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SeekParameters
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
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
import tv.mycujoo.mls.cast.MediaItem
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.enum.C
import tv.mycujoo.mls.enum.MessageLevel
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

class VideoPlayerMediator(
    private var videoPlayerConfig: VideoPlayerConfig,
    private val viewHandler: IViewHandler,
    private val reactorSocket: IReactorSocket,
    private val dispatcher: CoroutineScope,
    private val dataManager: IDataManager,
    private val timelineMarkerActionEntities: List<TimelineMarkerEntity>,
    private val castContext: CastContext,
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

    private var castSession: CastSession? = null
    private lateinit var sessionManagerListener: SessionManagerListener<CastSession>
    private var mediaItem: MediaItem? = null
    private var playbackState: PlaybackState = PlaybackState.IDLE
    private var playbackLocation: PlaybackLocation = LOCAL

    /**endregion */

    /**region Initialization*/
    fun initialize(MLSPlayerView: MLSPlayerView, player: IPlayer, builder: MLSBuilder) {
        initCastListener()
        castSession = castContext.sessionManager.currentCastSession

        this.playerView = MLSPlayerView
        this.player = player

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

    fun reInitialize(MLSPlayerView: MLSPlayerView, builder: MLSBuilder) {
        val exoPlayer = Player.createExoPlayer(MLSPlayerView.context)
        player.create(
            createMediaFactory(MLSPlayerView.context),
            exoPlayer,
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

    private fun initCastListener() {
        sessionManagerListener = object : SessionManagerListener<CastSession> {
            override fun onSessionStarting(session: CastSession?) {
            }

            override fun onSessionStarted(session: CastSession?, sessionId: String?) {
                onApplicationConnected(session);
            }

            override fun onSessionStartFailed(session: CastSession?, error: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionResuming(session: CastSession?, sessionId: String?) {
            }

            override fun onSessionResumed(session: CastSession?, wasSuspended: Boolean) {
                onApplicationConnected(session)
            }


            override fun onSessionResumeFailed(session: CastSession?, error: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionSuspended(session: CastSession?, reason: Int) {
            }

            override fun onSessionEnding(session: CastSession?) {
            }

            override fun onSessionEnded(session: CastSession?, error: Int) {
                onApplicationDisconnected()
            }

            private fun onApplicationConnected(theSession: CastSession?) {
                requireNotNull(theSession)
                castSession = theSession
                if (mediaItem != null) {
                    if (playbackState == PlaybackState.PLAYING) {
                        player.pause()
                        loadRemoteMedia(player.currentPosition())
                        return
                    } else {
                        playbackState = PlaybackState.IDLE
                        updatePlaybackLocation(REMOTE)
                    }
                }
            }


            private fun onApplicationDisconnected() {
                updatePlaybackLocation(LOCAL)
                playbackState = PlaybackState.IDLE
            }

        }
    }

    fun onResume() {
        castContext.sessionManager.addSessionManagerListener(
            sessionManagerListener, CastSession::class.java
        )
    }

    fun onPause() {
        castContext.sessionManager.removeSessionManagerListener(
            sessionManagerListener, CastSession::class.java
        )
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
        updateMediaItem(event)
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
        playerView.hideEventInfoButton()
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
            updateMediaItem(event)


            play(event.streams.first())
            playerView.hideEventInfoDialog()
        } else {
            // display event info
            playerView.showEventInformationPreEventDialog()
        }
    }

    private fun play(stream: Stream) {

        if (stream.widevine?.fullUrl != null && stream.widevine?.licenseUrl != null) {
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
    private fun loadRemoteMedia(currentPosition: Long) {
        if (castSession == null || mediaItem == null) {
            Log.e("VideoPlayerMediator", "castSession or media-item is null")
            return
        }
        val remoteMediaClient: RemoteMediaClient = castSession!!.remoteMediaClient
            ?: return


        remoteMediaClient.load(
            MediaLoadRequestData.Builder()
                .setMediaInfo(buildMediaInfo(mediaItem!!.url, player.duration()))
                .setAutoplay(true)
                .setCurrentTime(currentPosition).build()
        )

    }

    private fun buildMediaInfo(url: String, duration: Long): MediaInfo {
        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
//        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, mSelectedMedia.getStudio())
        movieMetadata.putString(MediaMetadata.KEY_TITLE, "test title")
//        movieMetadata.addImage(WebImage(Uri.parse(mSelectedMedia.getImage(0))))
//        movieMetadata.addImage(WebImage(Uri.parse(mSelectedMedia.getImage(1))))
        return MediaInfo.Builder(url)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("videos/mp4")
            .setMetadata(movieMetadata)
            .setStreamDuration(duration)
            .build()
    }

    private fun updatePlaybackLocation(location: PlaybackLocation) {
        playbackLocation = location
        when (playbackLocation) {
            LOCAL -> {
                if (playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.BUFFERING) {
                } else {

                }
            }
            REMOTE -> {

            }
        }
    }

    private fun updateMediaItem(eventEntity: EventEntity) {
        eventEntity.streams.firstOrNull()?.fullUrl?.let { url ->
            mediaItem = MediaItem(url)
        }

    }


    /**endregion */

}
