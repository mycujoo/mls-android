package tv.mycujoo.mls.core

import tv.mycujoo.mls.model.AnnotationBundle

class AnnotationPublisherImpl : AnnotationPublisher {

    lateinit var listener: AnnotationListener
    override fun setAnnotationListener(annotationListener: AnnotationListener) {
        listener = annotationListener
    }

    override fun onNewAnnotationAvailable(annotationBundle: AnnotationBundle) {
        listener.onNewAnnotationAvailable(annotationBundle)
    }
}