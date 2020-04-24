package tv.mycujoo.mls.api

import android.content.Context
import android.net.Uri
import android.os.Handler
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
import tv.mycujoo.mls.network.Api
import tv.mycujoo.mls.network.RemoteApi
import tv.mycujoo.mls.widgets.OnTimeLineChangeListener
import tv.mycujoo.mls.widgets.PlayerWidget
import tv.mycujoo.mls.widgets.TimeLineSeekBar
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


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
//            if (hasDefaultPlayerController.not()){

//            }

            hasAnnotation = builder.hasAnnotation

            if (hasAnnotation) {
                initAnnotation()
            }
        }

    }

    private fun initAnnotation() {
        coordinator = Coordinator(api, AnnotationPublisherImpl())
        coordinator.initialize(exoPlayer!!, Handler())

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
        val service: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
        service.scheduleWithFixedDelay(
            Runnable {
                exoPlayer?.let {
                    timeLineSeekBar?.progress =
                        ((it.contentPosition.toDouble() / it.duration.toDouble()) * 100).toInt()
                }
            },
            1,
            1,
            TimeUnit.MILLISECONDS
        )
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
        playerWidget.setPlayer(exoPlayer)
    }

    override fun getPlayerController(): PlayerController {
        return controller
    }

    override fun getPlayerStatus(): PlayerStatus {
        return playerStatus
    }

    class Builder {
        internal var context: Context? = null
            private set

        internal var playerEventsListener: PlayerEventsListener? = null
            private set

        internal var hasDefaultController: Boolean = true
            private set
        internal var hasAnnotation: Boolean = true
            private set

        fun withContext(context: Context) = apply { this.context = context }
        fun setPlayerEvents(playerEvents: PlayerEvents) =
            apply { this.playerEventsListener = PlayerEventsListener(playerEvents) }

        fun defaultPlayerController(defaultController: Boolean) =
            apply { this.hasDefaultController = defaultController }

        fun hasAnnotation(hasAnnotation: Boolean) =
            apply { this.hasAnnotation = hasAnnotation }

        fun build() = MyCujooLiveServiceImpl(this)
    }

    companion object {

        const val PUBLIC_KEY = "pk_test_123"

    }

}