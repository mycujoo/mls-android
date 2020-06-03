package tv.mycujoo.mls.core

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.STATE_ENDED
import com.google.android.exoplayer2.Player.STATE_IDLE
import tv.mycujoo.mls.api.PlayerEvents

class PlayerEventsListener(private val playerEvents: PlayerEvents) : Player.EventListener {

    override fun onLoadingChanged(isLoading: Boolean) {
        playerEvents.onLoadingChanged(isLoading)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        playerEvents.onPlayerStateChanged(playWhenReady, playbackState)
        if (playbackState == STATE_ENDED) {
            playerEvents.onEnd()
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        playerEvents.onIsPlayingChanged(isPlaying)
        if (isPlaying) {
            playerEvents.onPlay()
        } else {
            playerEvents.onPause()
        }
        println("MLS-App PlayerEventsListener - onIsPlayingChanged() isPlaying-> $isPlaying")
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        playerEvents.onPlayerError(error)

    }

    override fun onSeekProcessed() {
        super.onSeekProcessed()
        println("MLS-App PlayerEventsListener - onSeekProcessed()")
    }

    override fun onPositionDiscontinuity(reason: Int) {
        super.onPositionDiscontinuity(reason)
        println("MLS-App PlayerEventsListener - onPositionDiscontinuity() reason -> $reason")
    }
}
