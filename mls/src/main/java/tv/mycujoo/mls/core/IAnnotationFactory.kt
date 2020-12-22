package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.Action

interface IAnnotationFactory {
    fun setActions(actions: List<Action>)
    fun build(buildPoint: BuildPoint)

    fun getCurrentActions(): List<Action>
}