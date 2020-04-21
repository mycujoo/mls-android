package tv.mycujoo.mls.api

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import tv.mycujoo.mls.core.PlayerEventsListener
import tv.mycujoo.mls.core.PlayerStatusImpl
import tv.mycujoo.mls.widgets.PlayerWidget


class MyCujooLiveServiceImpl private constructor(builder: Builder) : MyCujooLiveService {


    private var playWhenReady: Boolean = true
    private var playbackPosition: Long = -1L

    // ExoPlayer is nullable, so it can be released manually
    private var exoPlayer: SimpleExoPlayer? = null

    private var context: Context

    private lateinit var playerWidget: PlayerWidget

    private lateinit var controller: PlayerController
    private lateinit var playerStatus: PlayerStatus
    private lateinit var playerEvents: PlayerEvents
    private lateinit var playerEventsListener: PlayerEventsListener
    private var hasDefaultPlayerController = true

    init {
        checkNotNull(builder.context)
        checkNotNull(builder.playerEventsListener)
        this.context = builder.context!!
        exoPlayer = SimpleExoPlayer.Builder(context).build()

        exoPlayer?.let {

            controller = PlayerControllerImpl(it)
            playerStatus = PlayerStatusImpl(it)

            it.addListener(builder.playerEventsListener!!)

            hasDefaultPlayerController = builder.hasDefaultController
        }

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
    }


    override fun initializePlayer(
        playerWidget: PlayerWidget
    ) {
        setView(playerWidget)
        playerWidget.setPlayerControllerState(hasDefaultPlayerController)
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

        fun withContext(context: Context) = apply { this.context = context }
        fun setPlayerEvents(playerEvents: PlayerEvents) =
            apply { this.playerEventsListener = PlayerEventsListener(playerEvents) }

        fun defaultPlayerController(defaultController: Boolean) =
            apply { this.hasDefaultController = defaultController }

        fun build() = MyCujooLiveServiceImpl(this)
    }

    companion object {

        const val PUBLIC_KEY = "pk_test_123"

    }

}