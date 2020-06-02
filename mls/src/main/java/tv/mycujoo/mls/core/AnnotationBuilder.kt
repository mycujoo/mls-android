package tv.mycujoo.mls.core

import tv.mycujoo.mls.entity.AnnotationSourceData
import tv.mycujoo.mls.entity.actions.ActionWrapper

abstract class AnnotationBuilder {
    abstract fun setCurrentTime(time: Long, playing: Boolean)
    abstract fun addPendingAnnotations(pendingAnnotationList: List<AnnotationSourceData>)
    abstract fun buildPendingAnnotationsForCurrentTime()

    abstract fun buildRemovalAnnotationsUpToCurrentTime()

    abstract fun addPendingActions(actions: List<ActionWrapper>)
}