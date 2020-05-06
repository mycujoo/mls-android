package tv.mycujoo.mls.api

import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.net.Uri
import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import tv.mycujoo.mls.cordinator.Coordinator
import tv.mycujoo.mls.core.AnnotationPublisherImpl
import tv.mycujoo.mls.core.PlayerEventsListener
import tv.mycujoo.mls.core.PlayerStatusImpl
import tv.mycujoo.mls.entity.HighlightAction
import tv.mycujoo.mls.model.ConfigParams
import tv.mycujoo.mls.network.Api
import tv.mycujoo.mls.network.RemoteApi
import tv.mycujoo.mls.widgets.*


class MyCujooLiveServiceImpl private constructor(builder: Builder) : MyCujooLiveService {

    private var playWhenReady: Boolean = true
    private var playbackPosition: Long = -1L

    private var context: Context

    // ExoPlayer is nullable, so it can be released manually
    private var exoPlayer: SimpleExoPlayer? = null

    private var api: Api

    private lateinit var playerWidget: PlayerWidget

    private lateinit var controller: PlayerController
    private lateinit var playerStatus: PlayerStatus
    private lateinit var playerEvents: PlayerEvents
    private lateinit var playerEventsListener: PlayerEventsListener
    private var hasDefaultPlayerController = true
    private var hasAnnotation = true

    private lateinit var coordinator: Coordinator

    private lateinit var handler: Handler

    var highlightAdapter: HighlightAdapter? = null

    val highlightList = ArrayList<HighlightAction>(0)


    init {
        checkNotNull(builder.context)
        checkNotNull(builder.playerEventsListener)
        this.context = builder.context!!

        api = RemoteApi()

        exoPlayer = SimpleExoPlayer.Builder(context).build()

        exoPlayer?.let {

            controller = PlayerControllerImpl(it)
            playerStatus = PlayerStatusImpl(it)

            it.addListener(builder.playerEventsListener!!)

            hasDefaultPlayerController = builder.hasDefaultController

            builder.highlightListParams?.let { highlightListParams ->
                highlightList.addAll(ArrayList(getHighlightList()))
                initHighlightList(highlightListParams)
            }

            hasAnnotation = builder.hasAnnotation

            if (hasAnnotation) {
                initAnnotation()
            }
        }

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

    fun playVideo(uri: Uri) {
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

        exoPlayer?.prepare(mediaSource)
        exoPlayer?.playWhenReady = playWhenReady

        coordinator.onPlayVideo()

        handler.postDelayed({ }, 4000L)
    }


    override fun initializePlayer(
        playerWidget: PlayerWidget,
        timeLineSeekBar: TimeLineSeekBar?
    ) {
        setView(playerWidget)
        playerWidget.setPlayerControllerState(hasDefaultPlayerController)

        if (hasDefaultPlayerController.not()) {
            coordinator.timeLineSeekBar = timeLineSeekBar
            initTimeLine(timeLineSeekBar)
        }


        if (hasAnnotation) {
            coordinator.widget = playerWidget
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
        configParams: ConfigParams,
        decorView: View,
        actionBar: androidx.appcompat.app.ActionBar?
    ) {
        when (configParams.config.orientation) {
            ORIENTATION_PORTRAIT -> {
                playerWidget.screenMode(PlayerWidget.ScreenMode.PORTRAIT)
                if (configParams.portraitActionBar.not()) {
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
                playerWidget.screenMode(PlayerWidget.ScreenMode.LANDSCAPE)
                if (configParams.landscapeActionBar.not()) {
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

    override fun releasePlayer() {
        exoPlayer?.let {
            playWhenReady = it.playWhenReady
            playbackPosition = it.currentPosition
            it.release()
            exoPlayer = null
        }
    }

    private fun setView(playerWidget: PlayerWidget) {
        this.playerWidget = playerWidget
        this.playerWidget.defaultController(hasDefaultPlayerController)
        playerWidget.setPlayer(exoPlayer)
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
        internal var context: Context? = null
            private set
        internal var hasDefaultController: Boolean = true
            private set
        internal var highlightListParams: HighlightListParams? = null
            private set
        internal var playerEventsListener: PlayerEventsListener? = null
            private set
        internal var hasAnnotation: Boolean = true
            private set

        fun withContext(context: Context) = apply { this.context = context }

        fun defaultPlayerController(defaultController: Boolean) =
            apply { this.hasDefaultController = defaultController }

        fun highlightList(highlightListParams: HighlightListParams) =
            apply { this.highlightListParams = highlightListParams }

        fun setPlayerEvents(playerEvents: PlayerEvents) =
            apply { this.playerEventsListener = PlayerEventsListener(playerEvents) }


        fun hasAnnotation(hasAnnotation: Boolean) =
            apply { this.hasAnnotation = hasAnnotation }

        fun build() = MyCujooLiveServiceImpl(this)

    }

    companion object {

        const val PUBLIC_KEY = "pk_test_123"

    }

}