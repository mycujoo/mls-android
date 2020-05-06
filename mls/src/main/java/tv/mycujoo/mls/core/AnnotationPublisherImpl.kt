package tv.mycujoo.mls.core

import tv.mycujoo.mls.entity.AnnotationSourceData


class AnnotationPublisherImpl : AnnotationPublisher {

    lateinit var listener: AnnotationListener
    override fun setAnnotationListener(annotationListener: AnnotationListener) {
        listener = annotationListener
    }

    override fun onNewAnnotationAvailable(annotationSourceData: AnnotationSourceData) {
        listener.onNewAnnotationAvailable(annotationSourceData)
    }
}