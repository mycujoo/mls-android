package tv.mycujoo.mcls.core

import com.google.android.exoplayer2.SimpleExoPlayer
import tv.mycujoo.mcls.api.PlayerStatus

class PlayerStatusImpl(private val exoPlayer: SimpleExoPlayer) : PlayerStatus {
    override fun getCurrentPosition(): Long {
        return exoPlayer.currentPosition
    }

    override fun getDuration(): Long {
        return exoPlayer.duration
    }
}