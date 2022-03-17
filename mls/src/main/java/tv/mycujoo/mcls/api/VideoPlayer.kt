package tv.mycujoo.mcls.api

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.STATE_READY
import timber.log.Timber
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.mcls.core.PlayerEventsListener
import tv.mycujoo.mcls.core.UIEventListener
import tv.mycujoo.mcls.core.VideoPlayerMediator
import tv.mycujoo.mcls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mcls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mcls.widgets.MLSPlayerView

class VideoPlayer(
    private val exoPlayer: ExoPlayer,
    private val videoPlayerMediator: VideoPlayerMediator,
    private val MLSPlayerView: MLSPlayerView
) : PlayerController, PlayerStatus, VideoPlayerContract {

    private var playerEventsListener: PlayerEventsListener? = null

    private lateinit var uiEventListener: UIEventListener
    private var optimisticSeekingPosition = -1

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                if (reason == STATE_READY) {
                    optimisticSeekingPosition = -1
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                if (playbackState == STATE_READY) {
                    optimisticSeekingPosition = -1
                }
            }
        })
    }

    /**region VideoPlayerContract: Player Higher level control*/
    override fun playVideo(event: EventEntity) {
        videoPlayerMediator.playVideo(event)
    }

    override fun playVideo(eventId: String) {
        videoPlayerMediator.playVideo(eventId)
    }

    override fun setLocalAnnotations(annotations: List<Action>) {
        videoPlayerMediator.setLocalActions(annotations)
    }

    override fun setPlayerEventsListener(listener: PlayerEventsListener) {
        playerEventsListener = listener
        exoPlayer.addListener(listener)
    }

    fun setOnConcurrencyControlExceeded(action: () -> Unit) {
        videoPlayerMediator.setOnConcurrencyControlExceeded(action)
    }

    override fun setUIEventListener(listener: UIEventListener) {
        uiEventListener = listener
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
        Timber.d("Seeking to $position, Current Track ${exoPlayer.currentMediaItem}")
        optimisticSeekingPosition = position

        exoPlayer.seekTo(position * ONE_SECOND_IN_MS)
        exoPlayer.play()
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
        return exoPlayer.volume == 0F
    }

    override fun mute() {
        exoPlayer.volume = 0F
    }

    override fun showEventInfoOverlay() {
        MLSPlayerView.showStartedEventInformationDialog()
    }

    override fun hideEventInfoOverlay() {
        MLSPlayerView.hideInfoDialogs()
    }

    override fun config(videoPlayerConfig: VideoPlayerConfig) {
        videoPlayerMediator.config(videoPlayerConfig)
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

    override fun isPlayingAd(): Boolean {
        return exoPlayer.isPlayingAd
    }
    /**endregion */
}
