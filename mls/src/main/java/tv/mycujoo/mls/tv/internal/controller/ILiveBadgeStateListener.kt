package tv.mycujoo.mls.tv.internal.controller

import tv.mycujoo.mls.widgets.MLSPlayerView

interface ILiveBadgeStateListener {
    fun setState(state: MLSPlayerView.LiveState)
}