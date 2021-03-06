package tv.mycujoo.mcls.mediator

import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.mcls.widgets.MLSPlayerView

interface IAnnotationMediator {
    fun initPlayerView(playerView: MLSPlayerView)
    fun release()

    var onSizeChangedCallback: () -> Unit

    fun fetchActions(
        timelineId: String,
        updateId: String? = null,
        resultCallback: ((result: Result<Exception, ActionResponse>) -> Unit)? = null
    )

    fun feed(actionResponse: ActionResponse)
    fun setLocalActions(annotations: List<Action>)
}