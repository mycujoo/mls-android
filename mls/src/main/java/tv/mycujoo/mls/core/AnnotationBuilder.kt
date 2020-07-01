package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.mls.entity.actions.ActionWrapper

abstract class AnnotationBuilder {
    abstract fun addPendingActionsDeprecated(actions: List<ActionWrapper>)
    abstract fun addPendingActions(actions: List<ActionEntity>)

    abstract fun setCurrentTime(time: Long, playing: Boolean)
    abstract fun buildPendingAnnotationsForCurrentTime()

    abstract fun buildRemovalAnnotationsUpToCurrentTime()

    abstract fun buildRemovalAnnotations()

}