package tv.mycujoo.mls.core

import com.google.android.exoplayer2.Player
import tv.mycujoo.mls.api.PlayerEvents

class PlayerEventsListener(private val playerEvents: PlayerEvents) : Player.EventListener {

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        playerEvents.onPlayerStateChanged(playbackState)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        playerEvents.onIsPlayingChanged(isPlaying)
    }
}
