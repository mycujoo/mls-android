package tv.mycujoo.mls.core

import tv.mycujoo.mls.entity.AnnotationSourceData
import tv.mycujoo.mls.entity.actions.ActionWrapper

interface AnnotationPublisher {
    fun setAnnotationListener(annotationListener: AnnotationListener)
    fun onNewAnnotationAvailable(annotationSourceData: AnnotationSourceData)

    fun onNewActionWrapperAvailable(actionWrapper: ActionWrapper)
}