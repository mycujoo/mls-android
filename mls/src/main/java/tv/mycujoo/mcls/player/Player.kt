package tv.mycujoo.mcls.player

import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Util
import tv.mycujoo.mcls.enum.C.Companion.DRM_WIDEVINE
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.ima.ImaCustomParams
import tv.mycujoo.mcls.utils.ThreadUtils
import javax.inject.Inject

/**
 * MLS video player, implementing IPlayer contract.
 * All video playing related functionality is done by this class
 * @see IPlayer
 */
class Player @Inject constructor(
    private val mediaFactory: MediaFactory,
    private var exoPlayer: ExoPlayer,
    private var mediaOnLoadCompletedListener: MediaOnLoadCompletedListener,
    private val threadUtils: ThreadUtils
) : IPlayer {

    /**region Fields*/

    private val handler = threadUtils.provideHandler()

    /**
     * IIma integration
     * can be null, if IMA module is not used
     */
    private var ima: IIma? = null

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
     */
    override fun create(ima: IIma?) {
        this.ima = ima
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
     * @return Exoplayer
     * Should be removed
     */
    override fun getDirectInstance(): ExoPlayer {
        return exoPlayer
    }

    /**
     * @return Exoplayer
     */
    override fun getPlayer(): Player {
        return exoPlayer
    }

    /**
     * Add listener to Exoplayer
     * @param eventListener implementation of EventListener
     */
    override fun addListener(eventListener: Player.Listener) {
        exoPlayer.addListener(eventListener)
    }

    /**
     * Seek to given offset
     * @param offset
     */
    override fun seekTo(offset: Long) {
        exoPlayer.seekTo(offset)
    }

    /**
     * Current position
     * @return current position if a media item has been loaded, or invalid otherwise
     */
    override fun currentPosition(): Long {
        return if (exoPlayer.currentMediaItemIndex >= 0) {
            exoPlayer.currentPosition
        } else {
            -1
        }
    }

    /**
     * Current absolute time
     * @return current absolute time if a media item has been loaded, or invalid otherwise
     */
    override fun currentAbsoluteTime(): Long {
        exoPlayer.let { exoplayer ->
            val dvrWindowStartTime = mediaOnLoadCompletedListener.getWindowStartTime()
            if (dvrWindowStartTime == -1L) {
                return -1L
            }
            return dvrWindowStartTime + exoplayer.currentPosition
        }
    }

    /**
     * Duration of current media item
     * @return duration of current media item, or invalid otherwise
     */
    override fun duration(): Long {
        return exoPlayer.duration
    }

    /**
     * Live status of current media item
     * @return true of it is live, or false otherwise
     */
    override fun isLive(): Boolean {
        return exoPlayer.isCurrentMediaItemDynamic && exoPlayer.contentPosition != 0L
    }

    /**
     * Playing status of current media item
     * @return true of it is playing media item, or false otherwise
     */
    override fun isPlaying(): Boolean {
        return exoPlayer.isPlaying
    }

    /**
     * Playing-Ad status of current media item
     * @return true of it is playing an Ad item, or false otherwise
     */
    override fun isPlayingAd(): Boolean {
        return exoPlayer.isPlayingAd
    }

    /**
     * Update resume position
     * Ensures latest position is retrievable after recreation
     */
    private fun updateResumePosition() {
        resumeWindow = exoPlayer.currentMediaItemIndex
        resumePosition = if (exoPlayer.isCurrentMediaItemSeekable) {
            0L.coerceAtLeast(exoPlayer.currentPosition)
        } else {
            C.POSITION_UNSET.toLong()
        }
    }

    /**
     * Release resources
     */
    override fun release() {
        exoPlayer.let {
            updateResumePosition()
            playWhenReady = it.playWhenReady
            playbackPosition = it.currentPosition

            it.release()
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

    override fun clearQue() {
        exoPlayer.clearMediaItems()
    }

    /**
     * Play media item
     * @param drmMediaData
     */
    override fun play(drmMediaData: MediaDatum.DRMMediaData) {
        this.mediaData = drmMediaData

        val config = MediaItem.DrmConfiguration
            .Builder(Util.getDrmUuid(DRM_WIDEVINE)!!)
            .setLicenseUri(drmMediaData.licenseUrl)
            .build()

        val mediaItem = MediaItem.Builder()
            .setDrmConfiguration(config)
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
            exoPlayer.seekTo(playbackPosition)
        }

        val haveResumePosition = resumeWindow != C.INDEX_UNSET
        if (haveResumePosition) {
            exoPlayer.let { simplePlayer ->
                simplePlayer.seekTo(resumeWindow, resumePosition)

                exoPlayer.let {
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
            exoPlayer.let {
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
        exoPlayer.playWhenReady = true
    }

    /**
     * Pause current media item
     */
    override fun pause() {
        exoPlayer.playWhenReady = false
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
        return mediaOnLoadCompletedListener.getWindowStartTime()
    }

    companion object {
        private const val TAG = "Player"
    }
}
