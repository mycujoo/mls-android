package tv.mycujoo.mls.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import tv.mycujoo.mls.enum.C.Companion.DRM_WIDEVINE
import tv.mycujoo.mls.ima.IIma
import tv.mycujoo.mls.ima.ImaCustomParams

class Player : IPlayer {

    private var exoPlayer: SimpleExoPlayer? = null
    private var ima: IIma? = null
    private lateinit var mediaFactory: MediaFactory
    private lateinit var handler: Handler
    private lateinit var mediaOnLoadCompletedListener: MediaOnLoadCompletedListener


    private var resumePosition: Long = C.INDEX_UNSET.toLong()
    private var resumeWindow: Int = C.INDEX_UNSET

    private var playWhenReady: Boolean = false
    private var playbackPosition: Long = -1L

    private var uri: Uri? = null
    private var dvrWindowSize: Long = Long.MAX_VALUE
    private var dvrWindowStartTime: Long = -1L
    private var licenseUrl: Uri? = null

    override fun create(
        ima: IIma?,
        mediaFactory: MediaFactory,
        exoPlayer: SimpleExoPlayer,
        handler: Handler,
        mediaOnLoadCompletedListener: MediaOnLoadCompletedListener
    ) {
        this.ima = ima
        this.mediaFactory = mediaFactory
        this.exoPlayer = exoPlayer
        this.handler = handler
        this.mediaOnLoadCompletedListener = mediaOnLoadCompletedListener
    }

    override fun isReady(): Boolean {
        return exoPlayer != null
    }

    override fun getDirectInstance(): ExoPlayer? {
        return exoPlayer
    }

    override fun getPlayer(): Player? {
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

    override fun currentAbsoluteTime(): Long {
        exoPlayer?.let { exoplayer ->
            dvrWindowStartTime = mediaOnLoadCompletedListener.getWindowStartTime()
            if (dvrWindowStartTime == -1L) {
                return -1L
            }

            return dvrWindowStartTime + exoplayer.currentPosition
        }

        return -1L
    }

    override fun duration(): Long {
        return exoPlayer?.duration ?: -1L
    }

    override fun isLive(): Boolean {
        exoPlayer?.let {
            return (it.isCurrentWindowDynamic && it.contentPosition != 0L)
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

    override fun play(drmMediaData: MediumData.DRMMediaData) {
        this.uri = Uri.parse(drmMediaData.fullUrl)
        this.dvrWindowSize = drmMediaData.dvrWindowSize
        this.licenseUrl = Uri.parse(drmMediaData.licenseUrl)

        val mediaItem = MediaItem.Builder()
            .setDrmUuid(Util.getDrmUuid(DRM_WIDEVINE))
            .setDrmLicenseUri(licenseUrl)
            .setUri(uri)
            .build()

        play(mediaItem, drmMediaData.autoPlay)
    }

    override fun play(mediaData: MediumData.MediaData) {
        this.uri = Uri.parse(mediaData.fullUrl)
        this.dvrWindowSize = mediaData.dvrWindowSize
        this.licenseUrl = null
        val mediaItem = mediaFactory.createMediaItem(mediaData.fullUrl)
        play(mediaItem, mediaData.autoPlay)
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
                val hlsMediaSource = mediaFactory.createHlsMediaSource(mediaItem)
                hlsMediaSource.addEventListener(handler, mediaOnLoadCompletedListener)



                if (ima != null) {
                    val adsMediaSource = ima!!.createMediaSource(
                        mediaFactory.defaultMediaSourceFactory,
                        hlsMediaSource,
                        ImaCustomParams()
                    )
                    it.setMediaSource(adsMediaSource, true)

                } else {
                    it.setMediaSource(hlsMediaSource, true)
                }


                it.prepare()
                it.playWhenReady = autoPlay
            }
        }

    }

    override fun play() {
        exoPlayer?.let {
            it.playWhenReady = true
        }
    }

    override fun pause() {
        exoPlayer?.let {
            it.playWhenReady = false
        }
    }

    override fun loadLastVideo() {
        if (licenseUrl != null) {
            play(
                MediumData.DRMMediaData(
                    fullUrl = uri.toString(),
                    dvrWindowSize = dvrWindowSize,
                    licenseUrl = licenseUrl.toString(),
                    autoPlay = false
                )
            )
        } else {
            uri?.let {
                play(
                    MediumData.MediaData(
                        fullUrl = it.toString(),
                        dvrWindowSize = dvrWindowSize,
                        autoPlay = false
                    )
                )
            }
        }
    }

    override fun isWithinValidSegment(targetAbsoluteTime: Long): Boolean? {
        if (exoPlayer == null) {
            return null
        }
        if (targetAbsoluteTime == -1L) {
            return null
        }
        return mediaOnLoadCompletedListener.getDiscontinuityBoundaries()
            .none { it.first <= targetAbsoluteTime && it.first + it.second >= targetAbsoluteTime }
    }


    override fun dvrWindowSize(): Long {
        return dvrWindowSize

    }

    override fun dvrWindowStartTime(): Long {
        exoPlayer?.let { _ ->
            dvrWindowStartTime = mediaOnLoadCompletedListener.getWindowStartTime()
        }

        return dvrWindowStartTime
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

        fun createDefaultMediaSourceFactory(
            context: Context
        ): DefaultMediaSourceFactory {
            return DefaultMediaSourceFactory(
                context
            )
        }
    }


}