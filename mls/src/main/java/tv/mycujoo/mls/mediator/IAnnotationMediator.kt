package tv.mycujoo.mls.mediator

import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.mls.widgets.MLSPlayerView

interface IAnnotationMediator {
    fun initPlayerView(playerView: MLSPlayerView)
    fun release()

    var onSizeChangedCallback: () -> Unit

    fun fetchActions(timelineId: String, resultCallback: ((result: Result<Exception, ActionResponse>) -> Unit)? = null)
    fun feed(actionResponse: ActionResponse)
}