package tv.mycujoo.mls.core

import tv.mycujoo.mls.entity.AnnotationSourceData
import tv.mycujoo.mls.entity.actions.AbstractAction
import tv.mycujoo.mls.entity.actions.ActionWrapper

abstract class AnnotationBuilder {
    abstract fun setCurrentTime(time: Long)
    abstract fun addPendingAnnotations(pendingAnnotationList: List<AnnotationSourceData>)
    abstract fun buildPendings()

    abstract fun addPendingActions(actions: List<ActionWrapper>)
}