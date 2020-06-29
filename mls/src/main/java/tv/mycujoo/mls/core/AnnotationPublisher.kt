package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.mls.entity.actions.ActionWrapper

interface AnnotationPublisher {
    fun setAnnotationListener(annotationListener: AnnotationListener)

    fun onNewActionWrapperAvailable(actionWrapper: ActionWrapper)
    fun onNewRemovalOrHidingActionAvailable(actionWrapper: ActionWrapper)

    fun onNewActionAvailable(actionEntity: ActionEntity)

}