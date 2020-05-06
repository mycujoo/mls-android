package tv.mycujoo.mls.core

import tv.mycujoo.mls.entity.AnnotationSourceData

abstract class AnnotationBuilder {
    abstract fun setCurrentTime(time: Long)
    abstract fun addPendingAnnotations(pendingAnnotationList: List<AnnotationSourceData>)
    abstract fun buildPendings()
}