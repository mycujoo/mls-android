package tv.mycujoo.mls.api

import com.google.android.exoplayer2.SimpleExoPlayer

class PlayerControllerImpl(private val exoPlayer: SimpleExoPlayer?) : PlayerController {
    override fun playerPlay() {
        exoPlayer?.playWhenReady = true
    }

    override fun playerPause() {
        exoPlayer?.playWhenReady = false
    }

    override fun playerNext() {
        exoPlayer?.next()
    }

    override fun playerPrevious() {
        exoPlayer?.previous()
    }
}