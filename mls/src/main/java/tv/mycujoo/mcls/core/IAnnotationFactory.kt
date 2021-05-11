package tv.mycujoo.mcls.core

import tv.mycujoo.domain.entity.Action

interface IAnnotationFactory {
    fun setActions(actions: List<Action>)
    fun build(buildPoint: BuildPoint)

    fun getCurrentActions(): List<Action>
    fun setLocalActions(annotations: List<Action>)
}