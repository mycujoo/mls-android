package tv.mycujoo.mls.core

import tv.mycujoo.mls.entity.actions.ActionWrapper

interface AnnotationListener {
    fun onNewActionWrapperAvailable(actionWrapper: ActionWrapper)

    fun onNewRemovalWrapperAvailable(actionWrapper: ActionWrapper)

}
