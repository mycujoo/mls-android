package tv.mycujoo.mls.tv.internal.controller

import com.google.android.exoplayer2.Player
import tv.mycujoo.mls.widgets.MLSPlayerView

class LiveBadgeStateHandler(val player: Player) {

    private lateinit var badgeStateListener: ILiveBadgeStateListener

    fun addListener(listenerBadge: ILiveBadgeStateListener) {
        this.badgeStateListener = listenerBadge
    }

    fun setLiveMode(state: MLSPlayerView.LiveState) {
        if (this::badgeStateListener.isInitialized.not()) {
            return
        }

        badgeStateListener.setState(state)
    }

    fun backToLive() {
        if (player.duration > 0) {
            player.seekTo(player.duration)
        }
    }


}