package tv.mycujoo.mcls.core

import tv.mycujoo.domain.entity.Action
import tv.mycujoo.mcls.widgets.MLSPlayerView

interface IAnnotationFactory {
    fun setActions(actions: List<Action>)
    fun build(buildPoint: BuildPoint, player: MLSPlayerView)

    fun getCurrentActions(): List<Action>
    fun setLocalActions(annotations: List<Action>)
}