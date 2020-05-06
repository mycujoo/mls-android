package tv.mycujoo.mls.core

import tv.mycujoo.mls.entity.AnnotationSourceData

interface AnnotationPublisher {
    fun setAnnotationListener(annotationListener: AnnotationListener)
    fun onNewAnnotationAvailable(annotationSourceData: AnnotationSourceData)
}