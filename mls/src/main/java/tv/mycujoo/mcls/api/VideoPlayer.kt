package tv.mycujoo.mcls.api

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.STATE_READY
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
        videoPlayerMediator.playVideo(event)
    }

    fun playVideo(eventId: String) {
        videoPlayerMediator.playVideo(eventId)
    }

    fun setLocalAnnotations(annotations: List<Action>){
        videoPlayerMediator.setLocalActions(annotations)
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

        exoPlayer.seekTo(position * ONE_SECOND_IN_MS)
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