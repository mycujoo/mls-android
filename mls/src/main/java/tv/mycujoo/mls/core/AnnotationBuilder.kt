package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.mls.entity.actions.ActionWrapper

abstract class AnnotationBuilder {
    @Deprecated("use @Class ActionEntity")
    abstract fun addPendingActionsDeprecated(actions: List<ActionWrapper>)

    /**
     * Add ActionEntity which will be processed to create Overlays
     */
    abstract fun addPendingActions(actions: List<ActionEntity>)

    abstract fun setCurrentTime(time: Long, playing: Boolean)
    abstract fun buildPendingAnnotationsForCurrentTime()
    abstract fun buildRemovalAnnotationsUpToCurrentTime()

    /**
     * Should return list of actions, which their 'offset' is passed, but not their duration [offset + duration]
     */
    abstract fun buildLingeringAnnotations()

    /**
     * Should return list of all Removal types of action from start to end
     */
    abstract fun buildRemovalAnnotations()

}