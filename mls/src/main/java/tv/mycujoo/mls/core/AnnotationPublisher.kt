package tv.mycujoo.mls.core

import tv.mycujoo.mls.model.AnnotationBundle

interface AnnotationPublisher {
    fun setAnnotationListener(annotationListener: AnnotationListener)
    fun onNewAnnotationAvailable(annotationBundle: AnnotationBundle)
}