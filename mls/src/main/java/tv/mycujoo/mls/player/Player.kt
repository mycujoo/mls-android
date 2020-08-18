package tv.mycujoo.mls.player

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util

class Player : IPlayer {

    private var exoPlayer: SimpleExoPlayer? = null
    private lateinit var mediaFactory: HlsMediaSource.Factory


    private var resumePosition: Long = C.INDEX_UNSET.toLong()
    private var resumeWindow: Int = C.INDEX_UNSET

    private var playWhenReady: Boolean = false
    private var playbackPosition: Long = -1L

    private var uri: Uri? = null

    override fun create(mediaFactory: HlsMediaSource.Factory, exoPlayer: SimpleExoPlayer) {
        this.mediaFactory = mediaFactory
        this.exoPlayer = exoPlayer
    }

    override fun isReady(): Boolean {
        return exoPlayer != null
    }

    override fun getDirectInstance(): ExoPlayer? {
        return exoPlayer
    }

    override fun addListener(eventListener: Player.EventListener) {
        exoPlayer?.addListener(eventListener)
    }

    override fun seekTo(offset: Long) {
        exoPlayer?.seekTo(offset)
    }

    override fun currentPosition(): Long {
        return exoPlayer?.currentPosition ?: -1L
    }

    override fun duration(): Long {
        return exoPlayer?.duration ?: -1L
    }

    override fun isLive(): Boolean {
        exoPlayer?.let {
            return (it.isCurrentWindowDynamic) || (it.duration == C.POSITION_UNSET.toLong())
        }

        return false

    }

    override fun isPlaying(): Boolean {
        exoPlayer?.let {
            return it.isPlaying
        }

        return false
    }

    private fun updateResumePosition() {
        if (exoPlayer == null) {
            return
        }
        resumeWindow = exoPlayer!!.currentWindowIndex
        resumePosition = if (exoPlayer!!.isCurrentWindowSeekable) Math.max(
            0,
            exoPlayer!!.currentPosition
        ) else C.POSITION_UNSET.toLong()
    }

    override fun release() {
        exoPlayer?.let {
            updateResumePosition()
            playWhenReady = it.playWhenReady
            playbackPosition = it.currentPosition

            it.release()
            exoPlayer = null
        }

    }


    override fun play(uriString: String) {
        this.uri = Uri.parse(uriString)
        this.uri?.let {
            play(it, true)
        }
    }

    private fun play(uri: Uri, playWhenReady: Boolean) {
        val mediaSource = mediaFactory.createMediaSource(uri)

        if (playbackPosition != -1L) {
            exoPlayer!!.seekTo(playbackPosition)
        }

        val haveResumePosition = resumeWindow != C.INDEX_UNSET
        if (haveResumePosition) {
            exoPlayer!!.seekTo(resumeWindow, resumePosition)
            exoPlayer?.prepare(mediaSource, false, false)
            resumePosition = C.INDEX_UNSET.toLong()
            resumeWindow = C.INDEX_UNSET
        } else {
            exoPlayer?.prepare(mediaSource, true, false)
        }

        exoPlayer?.playWhenReady = playWhenReady

    }

    override fun loadLastVideo() {
        uri?.let {
            play(it, false)
        }
    }

    companion object {
        fun createExoPlayer(context: Context): SimpleExoPlayer {
            return SimpleExoPlayer.Builder(context).build()
        }

        fun createMediaFactory(context: Context): HlsMediaSource.Factory {
            return HlsMediaSource.Factory(
                DefaultHttpDataSourceFactory(
                    Util.getUserAgent(
                        context,
                        "mls"
                    )
                )
            )
        }
    }


}