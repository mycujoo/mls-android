package tv.mycujoo.mls.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util

class Player : IPlayer {

    private var exoPlayer: SimpleExoPlayer? = null
    private lateinit var mediaFactory: HlsMediaSource.Factory
    private lateinit var mediaOnLoadCompletedListener: MediaOnLoadCompletedListener


    private var resumePosition: Long = C.INDEX_UNSET.toLong()
    private var resumeWindow: Int = C.INDEX_UNSET

    private var playWhenReady: Boolean = false
    private var playbackPosition: Long = -1L

    private var uri: Uri? = null
    private var licenseUrl: Uri? = null

    override fun create(
        mediaFactory: HlsMediaSource.Factory,
        exoPlayer: SimpleExoPlayer,
        mediaOnLoadCompletedListener: MediaOnLoadCompletedListener
    ) {
        this.mediaFactory = mediaFactory
        this.exoPlayer = exoPlayer
        this.mediaOnLoadCompletedListener = mediaOnLoadCompletedListener
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


    override fun play(uriString: String, autoPlay: Boolean) {
        this.uri = Uri.parse(uriString)
        val mediaItem = MediaItem.Builder().setUri(uri).build()
        play(mediaItem, autoPlay)

    }

    override fun play(uriString: String, licenseUrl: String, autoPlay: Boolean) {
        this.uri = Uri.parse(uriString)
        this.licenseUrl = Uri.parse(licenseUrl)

        val mediaItem = MediaItem.Builder()
            .setDrmUuid(Util.getDrmUuid("widevine"))
            .setDrmLicenseUri(licenseUrl)
            .setUri(uriString)
            .build()

        play(mediaItem, autoPlay)
    }

    private fun play(mediaItem: MediaItem, autoPlay: Boolean) {
        if (playbackPosition != -1L) {
            exoPlayer!!.seekTo(playbackPosition)
        }

        val haveResumePosition = resumeWindow != C.INDEX_UNSET
        if (haveResumePosition) {
            exoPlayer?.let {
                it.seekTo(resumeWindow, resumePosition)
                it.setMediaItem(mediaItem, false)
                it.prepare()
                it.playWhenReady = autoPlay
                resumePosition = C.INDEX_UNSET.toLong()
                resumeWindow = C.INDEX_UNSET
                it.playWhenReady = autoPlay
            }
        } else {
            exoPlayer?.let {
                val handler = Handler()

                val hlsMediaSource = mediaFactory.createMediaSource(mediaItem)
                hlsMediaSource.addEventListener(handler, mediaOnLoadCompletedListener)
                it.setMediaSource(hlsMediaSource, true)
                it.prepare()
                it.playWhenReady = autoPlay
            }
        }

    }

    override fun loadLastVideo() {
        if (licenseUrl != null) {
            play(uri.toString(), licenseUrl.toString(), false)
        } else {
            uri?.let {
                play(it.toString(), false)
            }
        }
    }

    override fun isInValidSegment(): Boolean {
        if (exoPlayer == null) {
            return false
        }
        val currentPosition = exoPlayer!!.currentPosition
        return mediaOnLoadCompletedListener.getDiscontinuityBoundaries()
            .any { it.first <= currentPosition && it.second >= currentPosition }
    }


    override fun dvrWindowSize(): Long {
        return mediaOnLoadCompletedListener.getDvrWindowDuration()
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