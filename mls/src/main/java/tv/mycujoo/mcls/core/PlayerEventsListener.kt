package tv.mycujoo.mcls.core

import com.google.android.exoplayer2.Player
import tv.mycujoo.mcls.api.PlayerEventsListener

/**
 * Implementation of Player.EventListener which dispatches core video-player events.
 * i.e. State changes and isPlaying changes.
 * @constructor accepts instance of PlayerEventsListener which is provided by user, and in turn,
 * this class will let user's listener know about events as well.
 * Each event might or might not be interest of MLS SDK and User. Hence these listeners are separated
 */
class PlayerEventsListener(private val playerEventsListener: PlayerEventsListener) :
    Player.EventListener {

    /**
     * called when (core) video player state changes
     * @param playWhenReady indicates if player should start/resume when content is loaded
     * @param playbackState player current state (Idle, Buffering, etc.) based on Exoplayer Player.State
     * @see Player.State
     */
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        playerEventsListener.onPlayerStateChanged(playbackState)
    }

    /**
     * called when playing state changes
     * @param isPlaying true when video player start playing, false otherwise
     */
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        playerEventsListener.onIsPlayingChanged(isPlaying)
    }
}
