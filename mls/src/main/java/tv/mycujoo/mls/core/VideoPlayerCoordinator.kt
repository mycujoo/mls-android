package tv.mycujoo.mls.core

import com.google.android.exoplayer2.Player.STATE_BUFFERING
import com.google.android.exoplayer2.SimpleExoPlayer
import tv.mycujoo.mls.widgets.PlayerViewWrapper

class VideoPlayerCoordinator(exoPlayer: SimpleExoPlayer, playerViewWrapper: PlayerViewWrapper) {

    init {
        val mainEventListener = object : MainEventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)

                if (playbackState == STATE_BUFFERING && playWhenReady) {
                    playerViewWrapper.showBuffering()
                } else {
                    playerViewWrapper.hideBuffering()
                }

            }
        }

        exoPlayer.addListener(mainEventListener)


    }

}
