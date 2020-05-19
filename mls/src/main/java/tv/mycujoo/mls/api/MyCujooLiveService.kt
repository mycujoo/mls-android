package tv.mycujoo.mls.api

import android.app.Activity
import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.npaw.youbora.lib6.YouboraLog
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2Adapter
import com.npaw.youbora.lib6.plugin.Options
import com.npaw.youbora.lib6.plugin.Plugin
import retrofit2.Retrofit
import tv.mycujoo.mls.BuildConfig
import tv.mycujoo.mls.analytic.YouboraClient
import tv.mycujoo.mls.cordinator.Coordinator
import tv.mycujoo.mls.core.AnnotationPublisherImpl
import tv.mycujoo.mls.core.PlayerEventsListener
import tv.mycujoo.mls.core.PlayerStatusImpl
import tv.mycujoo.mls.data.DataHolder
import tv.mycujoo.mls.di.DaggerMlsComponent
import tv.mycujoo.mls.di.NetworkModule
import tv.mycujoo.mls.entity.HighlightAction
import tv.mycujoo.mls.helper.TimeBarAnnotationHelper
import tv.mycujoo.mls.model.ConfigParams
import tv.mycujoo.mls.model.Event
import tv.mycujoo.mls.network.Api
import tv.mycujoo.mls.network.RemoteApi
import tv.mycujoo.mls.widgets.*
import javax.inject.Inject


class MyCujooLiveService private constructor(builder: Builder) : MyCujooLiveServiceInterface() {

    private var uri: Uri? = null
    private var resumePosition: Long = C.INDEX_UNSET.toLong()
    private var resumeWindow: Int = C.INDEX_UNSET

    private lateinit var playerView: PlayerView
    private var playWhenReady: Boolean = true
    private var playbackPosition: Long = -1L

    private var context: Context

    // ExoPlayer is nullable, so it can be released manually
    private var exoPlayer: SimpleExoPlayer? = null

    private var api: Api

    private lateinit var playerViewWrapper: PlayerViewWrapper

    private lateinit var controller: PlayerController
    private lateinit var playerStatus: PlayerStatus
    private lateinit var playerEvents: PlayerEvents
    private var playerEventsListener: PlayerEventsListener? = null
    private var hasDefaultPlayerController = true
    private var hasAnnotation = true
    private var hasAnalytic = false

    private lateinit var coordinator: Coordinator

    private lateinit var handler: Handler

    private var highlightAdapter: HighlightAdapter? = null

    private val highlightList = ArrayList<HighlightAction>(0)


    private lateinit var youboraClient: YouboraClient


    private val dataHolder = DataHolder()

    @Inject
    lateinit var retrofitClient : Retrofit


    init {
        checkNotNull(builder.context)
//        checkNotNull(builder.playerEventsListener)
        this.dataHolder
        this.context = builder.context!!

        api = RemoteApi()

        exoPlayer = SimpleExoPlayer.Builder(context).build()

        exoPlayer?.let {

            val dependencyGraph =
                DaggerMlsComponent.builder().networkModule(NetworkModule(builder.context!!)).build()

            dependencyGraph.inject(this)

            controller = PlayerControllerImpl(it)
            playerStatus = PlayerStatusImpl(it)

            playerEventsListener?.let { playerEventsListener -> it.addListener(playerEventsListener) }

            hasDefaultPlayerController = builder.hasDefaultController

            builder.highlightListParams?.let { highlightListParams ->
                highlightList.addAll(ArrayList(getHighlightList()))
                initHighlightList(highlightListParams)
            }

            hasAnnotation = builder.hasAnnotation

            if (hasAnnotation) {
                initAnnotation()
            }

            hasAnalytic = builder.hasAnalytic
            if (hasAnalytic) {
                initAnalytic(builder.publicKey, builder.activity, it)
            }
        }

    }

    private fun initAnalytic(
        publicKey: String,
        activity: Activity?,
        exoPlayer: SimpleExoPlayer
    ) {
        if (BuildConfig.DEBUG) {
            YouboraLog.setDebugLevel(YouboraLog.Level.VERBOSE)
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

    private fun initHighlightList(highlightListParams: HighlightListParams) {
        checkNotNull(highlightListParams.recyclerView)
        highlightAdapter = HighlightAdapter(ArrayList(getHighlightList()))
        highlightListParams.recyclerView.adapter = highlightAdapter
        highlightListParams.recyclerView.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        connectToHighlightList(highlightAdapter!!)
    }

    private fun initAnnotation() {
        handler = Handler()

        coordinator = Coordinator(api, AnnotationPublisherImpl())
        coordinator.initialize(exoPlayer!!, handler, highlightAdapter)

    }

    fun loadVideo(uri: Uri) {
        this.uri = uri
        dataHolder.eventLiveData = Event(
            "101",
            uri.toString(),
            "Sample name",
            "Sample location",
            "started"
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

        if (hasAnalytic) {
            youboraClient.logEvent(dataHolder.getEvent())
        }
    }

    fun playVideo(uri: Uri) {
        this.uri = uri
        dataHolder.eventLiveData = (
                Event(
                    "101",
                    uri.toString(),
                    "Sample name",
                    "Sample location",
                    "started"
                )
                )

        val mediaSource =
            HlsMediaSource.Factory(DefaultHttpDataSourceFactory(Util.getUserAgent(context, "mls")))
                .createMediaSource(uri)

        // Hls data source
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, "mls")
        )
        // todo: other media types data source
        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)


        if (playbackPosition != -1L) {
            exoPlayer?.seekTo(playbackPosition)
        }

        val haveResumePosition = resumeWindow != C.INDEX_UNSET
        if (haveResumePosition) {
            exoPlayer?.seekTo(resumeWindow, resumePosition)
        }

        exoPlayer?.prepare(mediaSource)
        exoPlayer?.playWhenReady = playWhenReady

        coordinator.onPlayVideo()

        if (hasAnalytic) {
            youboraClient.logEvent(dataHolder.getEvent())
        }
    }


    override fun initializePlayer(
        playerViewWrapper: PlayerViewWrapper,
        timeLineSeekBar: TimeLineSeekBar?
    ) {

        if (exoPlayer == null) {

            exoPlayer = SimpleExoPlayer.Builder(context).build()

            exoPlayer?.let {

                it.playWhenReady = playWhenReady

                val haveResumePosition = resumeWindow != C.INDEX_UNSET
                if (haveResumePosition) {
                    exoPlayer?.seekTo(resumeWindow, resumePosition)
                }
                uri?.let { uri ->
                    val mediaSource = HlsMediaSource.Factory(
                        DefaultHttpDataSourceFactory(
                            Util.getUserAgent(
                                context,
                                "mls"
                            )
                        )
                    )
                        .createMediaSource(uri)

                    it.prepare(mediaSource, false, false)
                }
            }


            this.playerViewWrapper = playerViewWrapper
            attachPlayer(playerViewWrapper)
        } else {

        }


//        playerViewWrapper.previewTimeBar.addOnPreviewChangeListener(object :
//            OnPreviewChangeListener {
//            override fun onStartPreview(previewView: PreviewView, progress: Int) {
//
//            }
//
//            override fun onPreview(previewView: PreviewView, progress: Int, fromUser: Boolean) {
//
//            }
//
//            override fun onStopPreview(previewView: PreviewView, progress: Int) {
//
//            }
//        })


        if (hasDefaultPlayerController.not()) {
            coordinator.timeLineSeekBar = timeLineSeekBar
            initTimeLine(timeLineSeekBar)
        }


        if (hasAnnotation) {
            coordinator.playerViewWrapper = playerViewWrapper
        }

        if (hasAnalytic) {

        }
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

    override fun onConfigurationChanged(
        config: ConfigParams,
        decorView: View,
        actionBar: androidx.appcompat.app.ActionBar?
    ) {
        when (config.config.orientation) {
            ORIENTATION_PORTRAIT -> {
                playerViewWrapper.screenMode(PlayerWidget.ScreenMode.PORTRAIT)
                if (config.hasPortraitActionBar.not()) {
                    decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                    actionBar?.hide()
                } else {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    actionBar?.show()

                }

            }
            ORIENTATION_LANDSCAPE -> {
                playerViewWrapper.screenMode(PlayerWidget.ScreenMode.LANDSCAPE)
                if (config.hasLandscapeActionBar.not()) {
                    decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                    actionBar?.hide()
                } else {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    actionBar?.show()

                }
            }
            else -> {
                //do nothing
            }
        }
    }

    override fun onStart(playerViewWrapper: PlayerViewWrapper) {
        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
            this.playerViewWrapper = playerViewWrapper
            attachPlayer(playerViewWrapper)
            initializePlayer(playerViewWrapper)
            if (hasAnalytic) {
                youboraClient.start()
            }
        }
    }

    override fun onResume(playerViewWrapper: PlayerViewWrapper) {
        if (Util.SDK_INT < Build.VERSION_CODES.N) {
            this.playerViewWrapper = playerViewWrapper
            attachPlayer(playerViewWrapper)
            initializePlayer(playerViewWrapper)
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

        playerViewWrapper.timeBarAnnotationHelper =
            TimeBarAnnotationHelper(api.getTimeLineMarkers())
    }

    override fun getPlayerController(): PlayerController {
        return controller
    }

    override fun getPlayerStatus(): PlayerStatus {
        return playerStatus
    }

    override fun getHighlightList(): List<HighlightAction> {
        return api.getHighlights()
    }

    private fun connectToHighlightList(highlightAdapter: HighlightAdapter) {
        val highlightClickListener = object : ListClickListener {
            override fun onClick(pos: Int) {
                exoPlayer?.seekTo(getHighlightList()[pos].streamOffset)
                exoPlayer?.playWhenReady = true
            }
        }
        highlightAdapter.setOnClickListener(highlightClickListener)
    }


    class Builder {
        internal var publicKey: String = ""
            private set
        internal var context: Context? = null
            private set
        internal var activity: Activity? = null
            private set
        internal var hasDefaultController: Boolean = true
            private set
        internal var highlightListParams: HighlightListParams? = null
            private set
        internal var playerEventsListener: PlayerEventsListener? = null
            private set
        internal var hasAnnotation: Boolean = true
            private set
        internal var hasAnalytic: Boolean = true
            private set

        fun publicKey(publicKey: String) = apply { this.publicKey = publicKey }

        fun withActivity(activity: Activity) = apply { this.activity = activity }

        fun withContext(context: Context) = apply { this.context = context }

        fun defaultPlayerController(defaultController: Boolean) =
            apply { this.hasDefaultController = defaultController }

        fun highlightList(highlightListParams: HighlightListParams) =
            apply { this.highlightListParams = highlightListParams }

        fun setPlayerEvents(playerEvents: PlayerEvents) =
            apply { this.playerEventsListener = PlayerEventsListener(playerEvents) }


        fun hasAnnotation(hasAnnotation: Boolean) =
            apply { this.hasAnnotation = hasAnnotation }

        fun hasAnalyticPlugin(hasAnalytic: Boolean) =
            apply { this.hasAnalytic = hasAnalytic }

        fun build() = MyCujooLiveService(this)

    }

    companion object {

        const val PUBLIC_KEY = "pk_test_123"
    }

}