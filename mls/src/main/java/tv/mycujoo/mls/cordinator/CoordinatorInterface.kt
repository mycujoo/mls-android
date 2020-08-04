package tv.mycujoo.mls.cordinator

import tv.mycujoo.mls.widgets.PlayerViewWrapper

interface CoordinatorInterface {
    fun initPlayerView(playerViewWrapper: PlayerViewWrapper)

    var onSizeChangedCallback: () -> Unit
}