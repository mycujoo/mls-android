package tv.mycujoo.mcls.player

import android.content.Context
import android.os.Handler
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import tv.mycujoo.mcls.enum.C.Companion.DRM_WIDEVINE
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.ima.ImaCustomParams
import javax.inject.Inject

/**
 * MLS video player, implementing IPlayer contract.
 * All video playing related functionality is done by this class
 * @see IPlayer
 */
class Player @Inject constructor(
    val mediaFactory: MediaFactory
) : IPlayer {

    /**region Fields*/
    /**
     * Exoplayer instance
     */
    private var exoPlayer: ExoPlayer? = null

    /**
     * IIma integration
     * can be null, if IMA module is not used
     */
    private var ima: IIma? = null

    /**
     * Handler to offload process from main thread to avoid UI blockage
     */
    private lateinit var handler: Handler

    /**
     * Callback for processing media items after they are loaded by Exoplayer,
     * meaning their tags are ready to be parsed.
     */
    private lateinit var mediaOnLoadCompletedListener: MediaOnLoadCompletedListener

    /**
     * Latest resume position at playing, if video player is playing.
     * This value is invalid if there is no item being played
     */
    private var resumePosition: Long = C.INDEX_UNSET.toLong()

    /**
     * Latest window of playing, if video player is playing.
     * This value is invalid if there is no item being played
     */
    private var resumeWindow: Int = C.INDEX_UNSET

    /**
     * Indicator that shows player should automatically start playing the item,
     * as soon as it's loaded
     */
    private var playWhenReady: Boolean = false

    /**
     * Current playback position, if video player is playing.
     * This value is invalid if there is no item being played
     */
    private var playbackPosition: Long = -1L

    /**
     * information of the item being played
     */
    private var mediaData: MediaDatum? = null
    /**endregion */


    /**
     * Create a ready-to-use Player by setting all the given properties
     * @param ima IMA integration, if Ima module is used
     * @param mediaFactory factory for creating media items
     * @param exoPlayer exoplayer used for playing video
     * @param handler handler for running jobs on other thread than Main thread
     * @param mediaOnLoadCompletedListener callback for sending data after media item is loaded by exoplayer
     */
    override fun create(
        ima: IIma?,
        mediaFactory: MediaFactory,
        exoPlayer: ExoPlayer,
        handler: Handler,
        mediaOnLoadCompletedListener: MediaOnLoadCompletedListener
    ) {
        this.ima = ima
        this.exoPlayer = exoPlayer
        this.handler = handler
        this.mediaOnLoadCompletedListener = mediaOnLoadCompletedListener
    }

    /**
     * re-initialize Player by setting those properties that are life-cycle bound
     * @param exoPlayer exoplayer used for playing video
     */
    override fun reInit(exoPlayer: ExoPlayer) {
        this.exoPlayer = exoPlayer
        this.mediaOnLoadCompletedListener = MediaOnLoadCompletedListener(exoPlayer)

    }

    /**
     * @return true if player is ready to play, false otherwise
     */
    override fun isReady(): Boolean {
        return exoPlayer != null
    }

    /**
     * @return Exoplayer
     * Should be removed
     */
    override fun getDirectInstance(): ExoPlayer? {
        return exoPlayer
    }

    /**
     * @return Exoplayer
     */
    override fun getPlayer(): Player? {
        return exoPlayer
    }

    /**
     * Add listener to Exoplayer
     * @param eventListener implementation of EventListener
     */
    override fun addListener(eventListener: Player.Listener) {
        exoPlayer?.addListener(eventListener)
    }

    /**
     * Seek to given offset
     * @param offset
     */
    override fun seekTo(offset: Long) {
        exoPlayer?.seekTo(offset)
    }

    /**
     * Current position
     * @return current position if a media item has been loaded, or invalid otherwise
     */
    override fun currentPosition(): Long {
        return exoPlayer?.currentPosition ?: -1L
    }

    /**
     * Current absolute time
     * @return current absolute time if a media item has been loaded, or invalid otherwise
     */
    override fun currentAbsoluteTime(): Long {
        exoPlayer?.let { exoplayer ->
            val dvrWindowStartTime = mediaOnLoadCompletedListener.getWindowStartTime()
            if (dvrWindowStartTime == -1L) {
                return -1L
            }
            return dvrWindowStartTime + exoplayer.currentPosition
        }

        return -1L
    }

    /**
     * Duration of current media item
     * @return duration of current media item, or invalid otherwise
     */
    override fun duration(): Long {
        return exoPlayer?.duration ?: -1L
    }

    /**
     * Live status of current media item
     * @return true of it is live, or false otherwise
     */
    override fun isLive(): Boolean {
        exoPlayer?.let {
            return (it.isCurrentWindowDynamic && it.contentPosition != 0L)
        }
        return false
    }

    /**
     * Playing status of current media item
     * @return true of it is playing media item, or false otherwise
     */
    override fun isPlaying(): Boolean {
        exoPlayer?.let {
            return it.isPlaying
        }
        return false
    }

    /**
     * Playing-Ad status of current media item
     * @return true of it is playing an Ad item, or false otherwise
     */
    override fun isPlayingAd(): Boolean {
        exoPlayer?.let {
            return it.isPlayingAd
        }
        return false
    }

    /**
     * Update resume position
     * Ensures latest position is retrievable after recreation
     */
    private fun updateResumePosition() {
        if (exoPlayer == null) {
            return
        }
        resumeWindow = exoPlayer!!.currentWindowIndex
        resumePosition = if (exoPlayer!!.isCurrentWindowSeekable) {
            0L.coerceAtLeast(exoPlayer!!.currentPosition)
        } else {
            C.POSITION_UNSET.toLong()
        }
    }

    /**
     * Release resources
     */
    override fun release() {
        exoPlayer?.let {
            updateResumePosition()
            playWhenReady = it.playWhenReady
            playbackPosition = it.currentPosition

            it.release()
            exoPlayer = null
        }
    }

    /**
     * reset all values to default
     * Should only be called in onDestroy
     */
    override fun destroy() {
        resumePosition = C.INDEX_UNSET.toLong()
        resumeWindow = C.INDEX_UNSET
        playWhenReady = false
        playbackPosition = -1L
        mediaData = null
    }

    /**
     * Play media item
     * @param drmMediaData
     */
    override fun play(drmMediaData: MediaDatum.DRMMediaData) {
        this.mediaData = drmMediaData

        val mediaItem = MediaItem.Builder()
            .setDrmUuid(Util.getDrmUuid(DRM_WIDEVINE))
            .setDrmLicenseUri(drmMediaData.licenseUrl)
            .setUri(drmMediaData.fullUrl)
            .build()

        play(mediaItem, drmMediaData.autoPlay)
    }

    /**
     * Play media item
     * @param mediaData
     */
    override fun play(mediaData: MediaDatum.MediaData) {
        this.mediaData = mediaData
        val mediaItem = mediaFactory.createMediaItem(mediaData.fullUrl)
        play(mediaItem, mediaData.autoPlay)
    }

    /**
     * Play media item
     * @param mediaItem
     * @param autoPlay
     */
    private fun play(mediaItem: MediaItem, autoPlay: Boolean) {
        if (playbackPosition != -1L) {
            exoPlayer!!.seekTo(playbackPosition)
        }

        val haveResumePosition = resumeWindow != C.INDEX_UNSET
        if (haveResumePosition) {
            exoPlayer?.let { simplePlayer ->
                simplePlayer.seekTo(resumeWindow, resumePosition)

                exoPlayer?.let {
                    val hlsMediaSource = mediaFactory.createHlsMediaSource(mediaItem)
                    hlsMediaSource.addEventListener(handler, mediaOnLoadCompletedListener)
                    if (ima != null) {
                        val adsMediaSource = ima!!.createMediaSource(
                            mediaFactory.defaultMediaSourceFactory,
                            hlsMediaSource,
                            ImaCustomParams(
                                eventId = mediaData?.eventId,
                                streamId = mediaData?.streamId,
                                eventStatus = mediaData?.eventStatus
                            )
                        )
                        simplePlayer.setMediaSource(adsMediaSource, false)

                    } else {
                        simplePlayer.setMediaSource(hlsMediaSource, false)
                    }
                    simplePlayer.prepare()
                    simplePlayer.playWhenReady = autoPlay
                    resumePosition = C.INDEX_UNSET.toLong()
                    resumeWindow = C.INDEX_UNSET
                }
            }
        } else {
            exoPlayer?.let {
                val hlsMediaSource = mediaFactory.createHlsMediaSource(mediaItem)
                hlsMediaSource.addEventListener(handler, mediaOnLoadCompletedListener)


                if (ima != null) {
                    val adsMediaSource = ima!!.createMediaSource(
                        mediaFactory.defaultMediaSourceFactory,
                        hlsMediaSource,
                        ImaCustomParams(
                            eventId = mediaData?.eventId,
                            streamId = mediaData?.streamId,
                            eventStatus = mediaData?.eventStatus
                        )
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

    /**
     * Play current media item
     */
    override fun play() {
        exoPlayer?.let {
            it.playWhenReady = true
        }
    }

    /**
     * Pause current media item
     */
    override fun pause() {
        exoPlayer?.let {
            it.playWhenReady = false
        }
    }

    /**
     * Load last loaded media item
     */
    override fun loadLastVideo() {
        mediaData?.let {
            when (mediaData) {
                is MediaDatum.MediaData -> {
                    play(it as MediaDatum.MediaData)
                }
                is MediaDatum.DRMMediaData -> {
                    play(it as MediaDatum.DRMMediaData)
                }
                else -> {
                    // should not happen
                }
            }
        }
    }

    /**
     * Compare absolute time with current segment time range
     * @param targetAbsoluteTime any time in absolute time system
     */
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


    /**
     * Return current DVR window size
     * @return dvr-window-size of current media item, if it is loaded. Invalid otherwise
     */
    override fun dvrWindowSize(): Long {
        return mediaData?.dvrWindowSize ?: -1L

    }

    /**
     * Return current DVR window start time
     * @return dvr-window start time of current media item, if it is loaded. Invalid otherwise
     */
    override fun dvrWindowStartTime(): Long {
        exoPlayer?.let { _ ->
            return mediaOnLoadCompletedListener.getWindowStartTime()
        }
        return -1L
    }

    companion object {
        fun createExoPlayer(context: Context): ExoPlayer {
            return ExoPlayer.Builder(context)
                .setSeekBackIncrementMs(10000)
                .setSeekForwardIncrementMs(10000)
                .build()
        }

        fun createMediaFactory(): HlsMediaSource.Factory {
            return HlsMediaSource.Factory(
                // TODO: Removing this mess up the Annotation Timings, Investigate :)
                DefaultHttpDataSource.Factory()
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