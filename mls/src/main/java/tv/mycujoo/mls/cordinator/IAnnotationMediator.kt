package tv.mycujoo.mls.cordinator

import tv.mycujoo.mls.widgets.PlayerViewWrapper

interface IAnnotationMediator {
    fun initPlayerView(playerViewWrapper: PlayerViewWrapper)
    fun release()

    var onSizeChangedCallback: () -> Unit
}