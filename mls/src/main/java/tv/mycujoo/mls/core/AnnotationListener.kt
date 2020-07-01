package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.mls.entity.actions.ActionWrapper

interface AnnotationListener {
    fun onNewActionWrapperAvailable(actionWrapper: ActionWrapper)

    fun onNewRemovalWrapperAvailable(actionWrapper: ActionWrapper)

    fun onNewActionAvailable(actionEntity: ActionEntity)

    fun clearScreen(customIdList: List<String>)

}
