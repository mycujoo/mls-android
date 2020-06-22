package tv.mycujoo.mls.api

import com.google.android.exoplayer2.SimpleExoPlayer
import tv.mycujoo.mls.core.PlayerEventsListener
import tv.mycujoo.mls.core.UIEventListener

class VideoPlayer(private val exoPlayer: SimpleExoPlayer) : PlayerController, PlayerStatus {

    var playerEventsListener: PlayerEventsListener? = null
        set(value) {
            field = value
            value?.let { exoPlayer.addListener(it) }
        }

    lateinit var uiEventListener: UIEventListener


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