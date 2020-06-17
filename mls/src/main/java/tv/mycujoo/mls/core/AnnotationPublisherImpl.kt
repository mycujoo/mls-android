package tv.mycujoo.mls.core

import tv.mycujoo.mls.entity.actions.ActionWrapper


class AnnotationPublisherImpl : AnnotationPublisher {

    lateinit var listener: AnnotationListener
    override fun setAnnotationListener(annotationListener: AnnotationListener) {
        listener = annotationListener
    }

    override fun onNewActionWrapperAvailable(actionWrapper: ActionWrapper) {
        listener.onNewActionWrapperAvailable(actionWrapper)
    }

    override fun onNewRemovalOrHidingActionAvailable(actionWrapper: ActionWrapper) {
        listener.onNewRemovalWrapperAvailable(actionWrapper)

    }
}