package tv.mycujoo.mls.api

import android.net.Uri
import com.google.android.exoplayer2.SimpleExoPlayer
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.mls.core.PlayerEventsListener
import tv.mycujoo.mls.core.UIEventListener
import tv.mycujoo.mls.core.VideoPlayerCoordinator

class VideoPlayer(
    private val exoPlayer: SimpleExoPlayer,
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
//        videoPlayerCoordinator.playVideo(event, )
    }

    fun playVideo(uri: Uri) {
        videoPlayerCoordinator.playVideo(uri)
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

    override fun next() {
        exoPlayer.next()
    }

    override fun previous() {
        exoPlayer.previous()
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