package tv.mycujoo.mls.mediator

import tv.mycujoo.mls.widgets.MLSPlayerView

interface IAnnotationMediator {
    fun initPlayerView(playerView: MLSPlayerView)
    fun release()

    var onSizeChangedCallback: () -> Unit
}