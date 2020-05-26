package tv.mycujoo.mls.core

import tv.mycujoo.mls.entity.AnnotationSourceData
import tv.mycujoo.mls.entity.actions.ActionWrapper


class AnnotationPublisherImpl : AnnotationPublisher {

    lateinit var listener: AnnotationListener
    override fun setAnnotationListener(annotationListener: AnnotationListener) {
        listener = annotationListener
    }

    override fun onNewAnnotationAvailable(annotationSourceData: AnnotationSourceData) {
        listener.onNewAnnotationAvailable(annotationSourceData)
    }

    override fun onNewActionWrapperAvailable(actionWrapper: ActionWrapper) {
        listener.onNewActionWrapperAvailable(actionWrapper)
    }
}