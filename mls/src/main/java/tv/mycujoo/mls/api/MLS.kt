package tv.mycujoo.mls.api

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Handler
import androidx.test.espresso.idling.CountingIdlingResource
import com.caverock.androidsvg.SVG
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SeekParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2Adapter
import com.npaw.youbora.lib6.plugin.Options
import com.npaw.youbora.lib6.plugin.Plugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.usecase.GetActionsFromJSONUseCase
import tv.mycujoo.domain.usecase.GetEventsUseCase
import tv.mycujoo.mls.BuildConfig
import tv.mycujoo.mls.analytic.YouboraClient
import tv.mycujoo.mls.cordinator.Coordinator
import tv.mycujoo.mls.core.PlayerEventsListener
import tv.mycujoo.mls.core.UIEventListener
import tv.mycujoo.mls.core.VideoPlayerCoordinator
import tv.mycujoo.mls.data.DataHolder
import tv.mycujoo.mls.di.DaggerMlsComponent
import tv.mycujoo.mls.di.NetworkModule
import tv.mycujoo.mls.helper.AnimationFactory
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.helper.SVGAssetResolver
import tv.mycujoo.mls.manager.IPrefManager
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.model.Event
import tv.mycujoo.mls.model.Stream
import tv.mycujoo.mls.network.Api
import tv.mycujoo.mls.network.RemoteApi
import tv.mycujoo.mls.widgets.OnTimeLineChangeListener
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import tv.mycujoo.mls.widgets.TimeLineSeekBar
import javax.inject.Inject


class MLS private constructor(builder: Builder) : MLSAbstract() {


    /**region Exo-player fields*/
    // ExoPlayer is nullable, so it can be released manually
    private var exoPlayer: SimpleExoPlayer? = null

    private var resumePosition: Long = C.INDEX_UNSET.toLong()
    private var resumeWindow: Int = C.INDEX_UNSET

    private var playWhenReady: Boolean = false
    private var playbackPosition: Long = -1L
    /**endregion */

    /**region Initializing fields*/
    private var builder: Builder

    /**endregion */

    /**region DI fields*/
    @Inject
    lateinit var eventsRepository: tv.mycujoo.domain.repository.EventsRepository

    @Inject
    lateinit var dispatcher: CoroutineScope

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var dataProvider: DataProviderImpl

    @Inject
    lateinit var prefManager: IPrefManager

    /**endregion */

    /**region MLS fields*/
    private var context: Context

    private var api: Api

    private lateinit var playerViewWrapper: PlayerViewWrapper

    private lateinit var videoPlayer: VideoPlayer
    private var hasDefaultPlayerController = true
    private var hasAnnotation = true
    private var hasAnalytic = false

    private var uri: Uri? = null

    private lateinit var coordinator: Coordinator

    private lateinit var handler: Handler

    private val dataHolder = DataHolder()
    private var viewIdentifierManager: ViewIdentifierManager

    /**endregion */

    /**region Plugins*/
    private lateinit var youboraClient: YouboraClient

    /**endregion */

    /**region Initializing*/
    init {
        checkNotNull(builder.activity)
        this.dataHolder
        this.context = builder.activity!!
        this.builder = builder

        val dependencyGraph =
            DaggerMlsComponent.builder().networkModule(NetworkModule(context)).build()
        dependencyGraph.inject(this)

        viewIdentifierManager = ViewIdentifierManager(dispatcher, CountingIdlingResource("ViewIdentifierManager"))

        persistPublicKey(this.builder.publicKey)

        api = RemoteApi()

        initSvgRenderingLibrary()

    }

    private fun initSvgRenderingLibrary() {
        SVG.registerExternalFileResolver(
            SVGAssetResolver(this.context.assets)
        )
    }

    private fun initAnalytic(
        publicKey: String,
        activity: Activity?,
        exoPlayer: SimpleExoPlayer
    ) {
        if (BuildConfig.DEBUG) {
//            YouboraLog.setDebugLevel(YouboraLog.Level.VERBOSE)
        }
        val youboraOptions = Options()
        //todo : use mls specific Youbora account
        youboraOptions.accountCode = "mycujoodev"
        youboraOptions.isAutoDetectBackground = true

        val plugin = Plugin(youboraOptions, context)
        plugin.activity = activity
        plugin.adapter = Exoplayer2Adapter(exoPlayer)

        youboraClient = YouboraClient(publicKey, plugin)


    }

    private fun initAnnotation() {
        handler = Handler()

        val identifierManager = viewIdentifierManager
        coordinator = Coordinator(identifierManager, exoPlayer!!, okHttpClient)
    }


    private fun initializePlayer(
        playerViewWrapper: PlayerViewWrapper
    ) {

        if (exoPlayer == null) {

            exoPlayer = SimpleExoPlayer.Builder(context).build()

            exoPlayer?.let {

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


                dispatcher.launch {
                    when (val result = GetEventsUseCase(eventsRepository).execute()) {
                        is Result.Success -> {
                        }
                        is Result.NetworkError -> {
                        }
                        is Result.GenericError -> {
                        }
                    }
                }

                videoPlayer = VideoPlayer(it)

                builder.playerEventsListener?.let { playerEventsListener ->
                    it.addListener(playerEventsListener)
                    videoPlayer.playerEventsListener = playerEventsListener
                }
                builder.uiEventListener?.let { uiEventCallback ->
                    videoPlayer.uiEventListener = uiEventCallback
                    playerViewWrapper.uiEventListener = uiEventCallback

                }

                hasDefaultPlayerController = builder.hasDefaultController

                hasAnnotation = builder.hasAnnotation
                if (hasAnnotation) {
                    initAnnotation()
                }
                hasAnalytic = builder.hasAnalytic
                if (hasAnalytic) {
                    initAnalytic(builder.publicKey, builder.activity, it)
                }
            }

            this.playerViewWrapper = playerViewWrapper
            playerViewWrapper.onSizeChangedCallback = coordinator.onSizeChangedCallback

        }

        if (hasAnnotation) {
            coordinator.playerViewWrapper = playerViewWrapper
        }

        playerViewWrapper.idlingResource = viewIdentifierManager.idlingResource
        playerViewWrapper.prepare(OverlayViewHelper(AnimationFactory()), viewIdentifierManager)

//        dataProvider.fetchEvents()
    }

    private fun initTimeLine(timeLineSeekBar: TimeLineSeekBar?) {
        timeLineSeekBar?.listener = object : OnTimeLineChangeListener {
            override fun onChange(level: Int) {
                exoPlayer?.let {
                    it.seekTo((it.duration / 100) * level)
                }
            }
        }

        val timeLineSyncRunnable = object : Runnable {
            override fun run() {
                exoPlayer?.let {
                    timeLineSeekBar?.progress =
                        ((it.contentPosition.toDouble() / it.duration.toDouble()) * 100).toInt()
                    handler.postDelayed(this, 1000L)
                }
            }
        }

        handler.postDelayed(timeLineSyncRunnable, 1000L)
    }
    /**endregion */

    /**region Over-ridden Functions*/
    override fun onStart(playerViewWrapper: PlayerViewWrapper) {
        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
            this.playerViewWrapper = playerViewWrapper
            initializePlayer(playerViewWrapper)
            attachPlayer(playerViewWrapper)
            if (hasAnalytic) {
                youboraClient.start()
            }
        }
    }

    override fun onResume(playerViewWrapper: PlayerViewWrapper) {
        if (Util.SDK_INT < Build.VERSION_CODES.N) {
            this.playerViewWrapper = playerViewWrapper
            initializePlayer(playerViewWrapper)
            attachPlayer(playerViewWrapper)
            if (hasAnalytic) {
                youboraClient.start()
            }
        }
    }

    override fun onPause() {
        if (Util.SDK_INT < Build.VERSION_CODES.N) {
            release()
        }
    }

    override fun onStop() {
        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
            release()
        }
    }

    override fun getVideoPlayer(): VideoPlayer {
        return videoPlayer
    }


    override fun loadVideo(uri: Uri) {
        playVideo(uri, false)
    }

    override fun playVideo(uri: Uri) {
        playVideo(uri, true)
    }

    override fun loadVideo(event: EventEntity) {
        event.streams.firstOrNull()?.fullUrl?.let {
            playVideo(Uri.parse(it), false)
        } ?: displayPreviewModeWithEventInfo(event)

        setEventInfoToPlayerViewWrapper(event)
    }

    override fun playVideo(event: EventEntity) {
        event.streams.firstOrNull()?.fullUrl?.let {
            playVideo(Uri.parse(it), true)
        } ?: displayPreviewModeWithEventInfo(event)
    }

    override fun getDataProvider(): DataProvider {
        return dataProvider
    }

    /**endregion */

    /**region Exo-player Functions*/

    private fun playVideo(uri: Uri, playWhenReady: Boolean) {
        this.uri = uri
        dataHolder.eventLiveData = (
                Event(
                    "101",
                    Stream(listOf(uri)),
                    "Sample name",
                    "Sample location",
                    "started"
                )
                )

        val mediaSource =
            HlsMediaSource.Factory(DefaultHttpDataSourceFactory(Util.getUserAgent(context, "mls")))
                .createMediaSource(uri)

        if (playbackPosition != -1L) {
            exoPlayer?.seekTo(playbackPosition)
        }

        val haveResumePosition = resumeWindow != C.INDEX_UNSET
        if (haveResumePosition) {
            exoPlayer?.seekTo(resumeWindow, resumePosition)
        }

        exoPlayer?.prepare(mediaSource)
        exoPlayer?.playWhenReady = playWhenReady or this.playWhenReady


        if (hasAnalytic) {
            youboraClient.logEvent(dataHolder.getEvent())
        }
    }

    private fun release() {
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

    private fun updateResumePosition() {
        exoPlayer?.let {
            resumeWindow = it.currentWindowIndex
            resumePosition = if (it.isCurrentWindowSeekable) Math.max(
                0,
                it.currentPosition
            ) else C.POSITION_UNSET.toLong()
        }

    }

    private fun attachPlayer(
        playerViewWrapper: PlayerViewWrapper
    ) {
        playerViewWrapper.playerView.player = exoPlayer
        playerViewWrapper.defaultController(hasDefaultPlayerController)

        initVideoPlayerCoordinator(
            playerViewWrapper,
            exoPlayer
        )

        playerViewWrapper.screenMode(PlayerViewWrapper.ScreenMode.PORTRAIT)
    }

    private fun initVideoPlayerCoordinator(
        playerViewWrapper: PlayerViewWrapper,
        exoPlayer: SimpleExoPlayer?
    ) {
        exoPlayer?.let {
            VideoPlayerCoordinator(
                exoPlayer,
                playerViewWrapper,
                builder.mlsConfiguration.VideoPlayerConfig,
                GetActionsFromJSONUseCase.mappedActionCollections().timelineMarkerActionList
            )
        }
    }

    /**endregion */

    /**region msc Functions*/
    private fun persistPublicKey(publicKey: String) {
        prefManager.persist("PUBLIC_KEY", publicKey)
    }

    private fun displayPreviewModeWithEventInfo(event: EventEntity) {
        if (!this::playerViewWrapper.isInitialized) {
            return
        }

        playerViewWrapper.hideEventInfoButton()
        playerViewWrapper.displayEventInformationDialog(
            event.title,
            event.description,
            event.streams.isNotEmpty()
        )
    }

    private fun setEventInfoToPlayerViewWrapper(event: EventEntity) {
        if (!this::playerViewWrapper.isInitialized) {
            return
        }

        playerViewWrapper.setVideoInfo(event.title, event.description)
    }

    private fun hidePreviewMode() {
        if (!this::playerViewWrapper.isInitialized) {
            return
        }
        playerViewWrapper.hideEventInfoDialog()
    }
    /**endregion */

    /**region Inner-classes*/
    class Builder {
        internal var publicKey: String = ""
            private set
        internal var activity: Activity? = null
            private set
        internal var hasDefaultController: Boolean = true
            private set
        internal var highlightListParams: HighlightListParams? = null
            private set
        internal var playerEventsListener: PlayerEventsListener? = null
            private set
        internal var uiEventListener: UIEventListener? = null
            private set
        internal var mlsConfiguration: MLSConfiguration = MLSConfiguration()
            private set

        internal var hasAnnotation: Boolean = true
            private set
        internal var hasAnalytic: Boolean = true
            private set

        fun publicKey(publicKey: String) = apply { this.publicKey = publicKey }

        fun withActivity(activity: Activity) = apply { this.activity = activity }

        fun defaultPlayerController(defaultController: Boolean) =
            apply { this.hasDefaultController = defaultController }

        fun highlightList(highlightListParams: HighlightListParams) =
            apply { this.highlightListParams = highlightListParams }

        fun setPlayerEventsListener(playerEventsListener: tv.mycujoo.mls.api.PlayerEventsListener) =
            apply { this.playerEventsListener = PlayerEventsListener(playerEventsListener) }

        fun setUIEventListener(uiEventListener: UIEventListener) =
            apply { this.uiEventListener = uiEventListener }


        fun hasAnnotation(hasAnnotation: Boolean) =
            apply { this.hasAnnotation = hasAnnotation }

        fun hasAnalyticPlugin(hasAnalytic: Boolean) =
            apply { this.hasAnalytic = hasAnalytic }

        fun build() = MLS(this)
        fun setConfiguration(mlsConfiguration: MLSConfiguration) = apply {
            this.mlsConfiguration = mlsConfiguration
        }

    }

    companion object {

        const val PUBLIC_KEY = "pk_test_123"
    }
    /**endregion */

}