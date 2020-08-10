package tv.mycujoo.mls.api

import com.google.android.exoplayer2.SimpleExoPlayer
import tv.mycujoo.domain.entity.EventEntity

class PlayerControllerImpl(private val exoPlayer: SimpleExoPlayer?) : PlayerController {
    override fun play() {
        exoPlayer?.playWhenReady = true
    }

    override fun pause() {
        exoPlayer?.playWhenReady = false
    }

    override fun next() {
        exoPlayer?.next()
    }

    override fun previous() {
        exoPlayer?.previous()
    }

    override fun displayEventInfo(eventEntity: EventEntity) {
        // todo
    }
}