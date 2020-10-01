package tv.mycujoo.mls.api

import com.google.android.exoplayer2.ExoPlayer
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.mls.core.PlayerEventsListener
import tv.mycujoo.mls.core.UIEventListener
import tv.mycujoo.mls.core.VideoPlayerCoordinator
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig

class VideoPlayer(
    private val exoPlayer: ExoPlayer,
    private val videoPlayerCoordinator: VideoPlayerCoordinator
) : PlayerController, PlayerStatus {

    var playerEventsListener: PlayerEventsListener? = null
        set(value) {
            field = value
            value?.let { exoPlayer.addListener(it) }
        }

    lateinit var uiEventListener: UIEventListener

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