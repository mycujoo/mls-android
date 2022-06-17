package tv.mycujoo.mcls.tv.player

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackGlue
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.ui.AdViewProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.entity.Stream
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.analytic.AnalyticsClient
import tv.mycujoo.mcls.analytic.YouboraClient
import tv.mycujoo.mcls.analytic.VideoAnalyticsCustomData
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.core.AbstractPlayerMediator
import tv.mycujoo.mcls.core.IAnnotationFactory
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.DeviceType
import tv.mycujoo.mcls.enum.MessageLevel
import tv.mycujoo.mcls.enum.StreamStatus
import tv.mycujoo.mcls.helper.ViewersCounterHelper.Companion.isViewersCountValid
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.model.JoinTimelineParam
import tv.mycujoo.mcls.network.socket.IBFFRTSocket
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.player.MediaDatum
import tv.mycujoo.mcls.tv.api.MLSTvBuilder
import tv.mycujoo.mcls.tv.internal.controller.ControllerAgent
import tv.mycujoo.mcls.tv.internal.transport.MLSPlaybackSeekDataProvider
import tv.mycujoo.mcls.tv.internal.transport.MLSPlaybackTransportControlGlueImplKt
import tv.mycujoo.mcls.utils.DeviceUtils
import tv.mycujoo.mcls.utils.StringUtils
import tv.mycujoo.mcls.utils.ThreadUtils
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import tv.mycujoo.mcls.widgets.CustomInformationDialog
import tv.mycujoo.mcls.widgets.MLSPlayerView
import tv.mycujoo.mcls.widgets.PreEventInformationDialog
import tv.mycujoo.mcls.widgets.UiEvent
import tv.mycujoo.ui.MLSTVFragment
import javax.inject.Inject

class TvVideoPlayer @Inject constructor(
    @ApplicationContext val context: Context,
    private val reactorSocket: IReactorSocket,
    private val bffRtSocket: IBFFRTSocket,
    private val dispatcher: CoroutineScope,
    private val dataManager: IDataManager,
    private val logger: Logger,
    private val player: IPlayer,
    private val tvAnnotationMediator: TvAnnotationMediator,
    private val annotationFactory: IAnnotationFactory,
    private val analyticsClient: AnalyticsClient,
    private val controllerAgent: ControllerAgent,
    private val threadUtils: ThreadUtils,
    private val userPreferencesUtils: UserPreferencesUtils,
) : AbstractPlayerMediator(reactorSocket, bffRtSocket, dispatcher, logger) {

    lateinit var mMlsTvFragment: MLSTVFragment
    var ima: IIma? = null
    var mlsTVConfiguration: MLSTVConfiguration = MLSTVConfiguration()

    /**region Fields*/
    private lateinit var leanbackAdapter: LeanbackPlayerAdapter
    private lateinit var glueHost: VideoSupportFragmentGlueHost
    private lateinit var mTransportControlGlue: MLSPlaybackTransportControlGlueImplKt<LeanbackPlayerAdapter>
    private lateinit var eventInfoContainerLayout: FrameLayout
    private val dialogs = ArrayList<View>()

    private lateinit var overlayContainer: ConstraintLayout

    /**
     * Indicates if current video session is logged or not, for analytical purposes
     */
    private var logged = false

    /**endregion */

    /**
     * Indicates if SDK user desires to have analytics enabled
     */
    private var hasAnalytic = false

    /**
     * Latest updateId received from Reactor service, or null if not joined at all
     */
    private var updateId: String? = null

    /**
     * onConcurrencyLimitExceeded, the extension that the app can use to define it's own behaviour
     * when the limit has been exceeded
     */
    private var onConcurrencyLimitExceeded: ((Int) -> Unit)? = null

    /**
     * Retry action for ConcurrencyRequest
     */
    private val concurrencyRequestRetryHandler = threadUtils.provideHandler()
    private val concurrencyRequestRetryRunnable = Runnable {
        dataManager.currentEvent?.id?.let {
            Timber.d("Retry startWatch")
            startWatchSession(it)
        }
    }

    /**
     * Concurrency Limit Feature Toggle
     */
    var concurrencyLimitEnabled = true

    private var playerReady = false

    /**region Initializing*/
    fun initialize(mlsTvFragment: MLSTVFragment, builder: MLSTvBuilder) {
        this.mMlsTvFragment = mlsTvFragment
        this.ima = builder.ima
        this.onConcurrencyLimitExceeded = builder.onConcurrencyLimitExceeded
        this.concurrencyLimitEnabled = builder.concurrencyLimitFeatureEnabled

        // Initializers for Other Components
        annotationFactory.attachPlayerView(mlsTvFragment)
        val adViewProvider = addAdViewProvider(mMlsTvFragment.uiBinding.fragmentRoot)


        // IMA
        ima?.setAdViewProvider(adViewProvider)
        player.apply {
            create(
                ima,
            )
        }

        // Analytics
        hasAnalytic = builder.hasAnalytic
        if (builder.hasAnalytic) {
            initAnalytic(
                activity = builder.mlsTvFragment.requireActivity(),
                exoPlayer = this.player.getDirectInstance()!!,
                accountCode = builder.getAnalyticsCode(),
                videoAnalyticsCustomData = builder.videoAnalyticsCustomData,
                deviceType = builder.deviceType
            )
        }
        this.player.getDirectInstance()?.let { exoPlayer ->
            ima?.setPlayer(exoPlayer)
        }


        // Configurations
        player.getDirectInstance()?.let {
            it.playWhenReady = mlsTVConfiguration.videoPlayerConfig.autoPlay
            leanbackAdapter = LeanbackPlayerAdapter(context, it, 1000)

            glueHost = VideoSupportFragmentGlueHost(mMlsTvFragment.videoSupportFragment)

            Timber.d("initialize: Attached VideoSupportFragmentGlueHost and leanbackAdapter")
        }

        // Buffer Progress Bar
        val bufferProgressBar = ProgressBar(context)
        bufferProgressBar.indeterminateDrawable.setTint(Color.parseColor(mlsTVConfiguration.videoPlayerConfig.primaryColor))
        val layoutParams = FrameLayout.LayoutParams(120, 120)
        layoutParams.gravity = Gravity.CENTER
        bufferProgressBar.visibility = View.GONE
        mMlsTvFragment.uiBinding.fragmentRoot.addView(bufferProgressBar, layoutParams)


        player.getPlayer()?.let {
            controllerAgent.setBufferProgressBar(bufferProgressBar)
        }


        mTransportControlGlue = MLSPlaybackTransportControlGlueImplKt(
            context,
            leanbackAdapter,
            mlsTVConfiguration,
            controllerAgent
        )
        mTransportControlGlue.host = glueHost
        mTransportControlGlue.playWhenPrepared()

        if (mTransportControlGlue.isPrepared) {
            mTransportControlGlue.setSeekProvider(MLSPlaybackSeekDataProvider(5000L))
        } else {
            mTransportControlGlue.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
                override fun onPreparedStateChanged(glue: PlaybackGlue?) {
                    if (glue?.isPrepared == true) {
                        glue.removePlayerCallback(this)
                        val transportControlGlue =
                            glue as MLSPlaybackTransportControlGlueImplKt<*>
                        transportControlGlue.setSeekProvider(MLSPlaybackSeekDataProvider(5000L))
                    }
                }
            })
        }

        this.player.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    dataManager.currentEvent?.let { event ->
                        if (event.is_protected && event.isNativeMLS && concurrencyLimitEnabled) {
                            startWatchSession(event.id)
                        }
                    }

                    mTransportControlGlue.getSeekProvider()?.let {
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

                logEventIfNeeded(playbackState)
            }
        })

        if (mMlsTvFragment.view == null) {
            throw IllegalArgumentException("Fragment must be supported in a state which has active view!")
        } else {
            val rootView = mMlsTvFragment.uiBinding.fragmentRoot

            overlayContainer = ConstraintLayout(rootView.context)
            rootView.addView(
                overlayContainer,
                2,
                FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            )

            eventInfoContainerLayout = FrameLayout(rootView.context)
            rootView.addView(
                eventInfoContainerLayout,
                1,
                FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            )
        }

        playerReady = true
        playPendingEvent()
    }

    private fun addAdViewProvider(fragmentView: FrameLayout): AdViewProvider {
        val frameLayout = FrameLayout(fragmentView.context)
        fragmentView.addView(frameLayout, 0)
        return AdViewProvider { frameLayout }
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
        if (playbackState == Player.STATE_READY) {
            analyticsClient.logEvent(dataManager.currentEvent, player.isLive())
            logged = true
        }
    }

    /**
     * Stops Concurrency Limit for Future Events on Runtime
     */
    fun setConcurrencyLimitFeatureEnabled(enabled: Boolean) {
        concurrencyLimitEnabled = enabled

        if (concurrencyLimitEnabled.not()) {
            bffRtSocket.leaveCurrentSession()
        }
    }

    /**endregion */
    private fun initAnalytic(
        activity: Activity,
        exoPlayer: ExoPlayer,
        accountCode: String,
        deviceType: DeviceType?,
        videoAnalyticsCustomData: VideoAnalyticsCustomData?
    ) {
        if (analyticsClient is YouboraClient) {
            val device = deviceType ?: DeviceUtils.detectTVDeviceType(activity)

            analyticsClient.setYouboraPlugin(
                activity = activity,
                exoPlayer = exoPlayer,
                accountCode = accountCode,
                deviceType = device,
                videoAnalyticsCustomData = videoAnalyticsCustomData,
            )
        }
    }

    fun attachPlayer(playerView: MLSPlayerView) {
        playerView.playerView.player = player.getDirectInstance()
        playerView.playerView.hideController()

        if (hasAnalytic) {
            analyticsClient.start()
        }

    }

    fun getPlayerDirectInstance(): ExoPlayer? {
        return player.getDirectInstance()
    }

    fun setLocalAnnotations(annotations: List<Action>) {
        if (playerReady) {
            tvAnnotationMediator.setLocalActions(annotations)
        } else {
            val retry = Runnable {
                setLocalAnnotations(annotations)
            }

            threadUtils.provideHandler().postDelayed(retry, 500L)
        }
    }

    override fun onReactorEventUpdate(eventId: String, updateId: String) {
        cancelStreamUrlPulling()
        dispatcher.launch(context = Dispatchers.Main) {
            when (val result = dataManager.getEventDetails(eventId, updateId)) {
                is Result.Success -> {
                    dataManager.currentEvent = result.value
                    updateStreamStatus(result.value)
                    playVideoOrDisplayEventInfo(result.value)
                    startStreamUrlPullingIfNeeded(result.value)
                    fetchActions(result.value, true)
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

    private fun startWatchSession(eventId: String) {
        Timber.d("startWatchSession")
        bffRtSocket.startSession(eventId, userPreferencesUtils.getIdentityToken())
    }

    override fun onConcurrencyBadRequest(reason: String) {
        logger.log(MessageLevel.ERROR, reason)
    }

    override fun onConcurrencyLimitExceeded(allowedDevicesNumber: Int) {
        Timber.d("onConcurrencyLimitExceeded")
        if (concurrencyLimitEnabled) {
            threadUtils.provideHandler().post {
                streaming = false
                player.clearQue()
                annotationFactory.clearOverlays()
                onConcurrencyLimitExceeded?.invoke(allowedDevicesNumber)
            }
        }
    }

    override fun onConcurrencyServerError() {
        concurrencyRequestRetryHandler.postDelayed(concurrencyRequestRetryRunnable, 5000)
    }

    private fun fetchActions(event: EventEntity, joinTimeLine: Boolean) {
        if (event.timeline_ids.isEmpty()) {
            return
        }
        fetchActions(event.timeline_ids.first(), null, joinTimeLine)
    }

    private fun fetchActions(timelineId: String, updateId: String?, joinTimeline: Boolean) {
        dispatcher.launch(context = Dispatchers.Main) {
            when (val result = dataManager.getActions(timelineId, updateId)) {
                is Result.Success -> {

                    tvAnnotationMediator.feed(result.value)

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
    override fun playVideo(event: EventEntity) {
        dataManager.currentEvent = event

        if (playerReady) {
            updateStreamStatus(event)
            playVideoOrDisplayEventInfo(event)

            if (event.isNativeMLS) {
                joinEvent(event)
                startStreamUrlPullingIfNeeded(event)
                fetchActions(event, true)
            }
        } else {
            threadUtils.provideHandler().postDelayed({
                playVideo(event)
            }, 100)
        }
    }

    private fun playPendingEvent() {
        dataManager.currentEvent?.let {
            playVideo(it)
        }
    }

    override fun playVideo(eventId: String) {
        dispatcher.launch(context = Dispatchers.Main) {
            when (val result = dataManager.getEventDetails(eventId, updateId)) {
                is Result.Success -> {
                    playVideo(result.value)
                }
                is Result.NetworkError -> {
                    logger.log(
                        MessageLevel.DEBUG,
                        C.NETWORK_ERROR_MESSAGE.plus("${result.error}")
                    )
                }
                is Result.GenericError -> {
                    logger.log(
                        MessageLevel.ERROR,
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
                    logged = false

                    play(event.streams.first())
                    eventInfoContainerLayout.visibility = View.GONE
                    hideInfoDialogs()
                }
            }
            StreamStatus.GEOBLOCKED -> {
                streaming = false
                player.pause()
                showCustomInformationDialog(context.getString(R.string.message_geoblocked_stream))
            }
            StreamStatus.NO_ENTITLEMENT -> {
                streaming = false
                player.pause()
                showCustomInformationDialog(context.getString(R.string.message_no_entitlement_stream))
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

    /**
     * Release resources & leave Reactor service
     */
    fun release() {
        streaming = false
        player.release()
        tvAnnotationMediator.release()
        if (hasAnalytic) {
            analyticsClient.stop()
        }
        reactorSocket.leave(true)
        bffRtSocket.leaveCurrentSession()
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
                startTime = it.getFormattedStartTimeDate(),
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
                startTime = it.getFormattedStartTimeDate(),
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
