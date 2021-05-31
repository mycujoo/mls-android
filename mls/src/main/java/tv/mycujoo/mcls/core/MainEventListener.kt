package tv.mycujoo.mcls.core

import com.google.android.exoplayer2.Player

/**
 * Interface on ExoPlayer Player.EventListener to distinguish between MainEvents vs. UIEvents in
 * VideoPlayerMediator
 * @see VideoPlayerMediator
 */
interface MainEventListener : Player.EventListener
