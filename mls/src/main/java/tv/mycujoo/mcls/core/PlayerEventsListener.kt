package tv.mycujoo.mcls.core

import com.google.android.exoplayer2.Player
import tv.mycujoo.mcls.api.PlayerEventsListener

class PlayerEventsListener(private val playerEventsListener: PlayerEventsListener) : Player.EventListener {

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        playerEventsListener.onPlayerStateChanged(playbackState)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        playerEventsListener.onIsPlayingChanged(isPlaying)
    }
}
