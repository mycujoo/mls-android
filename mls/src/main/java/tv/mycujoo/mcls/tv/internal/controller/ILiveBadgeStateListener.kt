package tv.mycujoo.mcls.tv.internal.controller

import tv.mycujoo.mcls.widgets.MLSPlayerView

interface ILiveBadgeStateListener {
    fun setState(state: MLSPlayerView.LiveState)
}