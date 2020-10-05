package tv.mycujoo.mls.api

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.STATE_READY
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.mls.core.PlayerEventsListener
import tv.mycujoo.mls.core.UIEventListener
import tv.mycujoo.mls.core.VideoPlayerCoordinator
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.widgets.MLSPlayerView

class VideoPlayer(
    private val exoPlayer: ExoPlayer,
    private val videoPlayerCoordinator: VideoPlayerCoordinator,
    private val MLSPlayerView: MLSPlayerView
) : PlayerController, PlayerStatus {

    var playerEventsListener: PlayerEventsListener? = null
        set(value) {
            field = value
            value?.let { exoPlayer.addListener(it) }
        }

    lateinit var uiEventListener: UIEventListener
    private var optimisticSeekingPosition = -1

    init {
        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if (playbackState == STATE_READY) {
                    optimisticSeekingPosition = -1
                }
            }
        })
    }


    /**region Player Higher level control*/
    fun playVideo(event: EventEntity) {
        videoPlayerCoordinator.playVideo(event)
    }

    fun playVideo(eventId: String) {
        videoPlayerCoordinator.playVideo(eventId)
    }

    fun playExternalSourceVideo(videoUrl: String) {
        videoPlayerCoordinator.playExternalSourceVideo(videoUrl)
    }
    /**endregion */


    /**region PlayerController*/
    fun getPlayerController(): PlayerController {
        return this
    }

    override fun play() {
        exoPlayer.playWhenReady = true
    }

    override fun pause() {
        exoPlayer.playWhenReady = false
    }

    override fun seekTo(position: Int) {
        optimisticSeekingPosition = position

        exoPlayer.seekTo(position * 1000L)
    }

    override fun currentTime(): Int {
        val currentTime = exoPlayer.currentPosition.toInt()
        if (currentTime < 0) {
            return -1
        }
        return currentTime / 1000
    }

    override fun optimisticCurrentTime(): Int {
        return if (optimisticSeekingPosition != -1) {
            optimisticSeekingPosition
        } else currentTime()
    }

    override fun currentDuration(): Int {
        val duration = exoPlayer.duration.toInt()
        if (duration < 0) {
            return -1
        }
        return duration / 1000
    }

    override fun isMuted(): Boolean {
        return exoPlayer.audioComponent?.volume == 0F
    }

    override fun mute() {
        exoPlayer.audioComponent?.volume = 0F
    }

    override fun showEventInfoOverlay() {
        MLSPlayerView.displayEventInfoForStartedEvents()
    }

    override fun hideEventInfoOverlay() {
        MLSPlayerView.hideEventInfoDialog()
    }

    override fun config(videoPlayerConfig: VideoPlayerConfig) {
        videoPlayerCoordinator.config(videoPlayerConfig)
    }

    /**endregion */

    /**region PlayerStatus*/
    fun getPlayerStatus(): PlayerStatus {
        return this
    }

    override fun getCurrentPosition(): Long {
        return exoPlayer.currentPosition
    }

    override fun getDuration(): Long {
        return exoPlayer.duration
    }
    /**endregion */


}