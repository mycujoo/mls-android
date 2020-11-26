package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.domain.entity.HideOverlayAct

class HideOverlayActionHelper {
    companion object {
        fun getOverlayActionCurrentAct(
            timeSystem: TimeSystem,
            currentTime: Long,
            actionObject: ActionObject
        ): HideOverlayAct {
            if (ShowOverlayActionHelper.outroIsInCurrentTimeRange(
                    timeSystem,
                    currentTime,
                    actionObject
                )
            ) {
                return HideOverlayAct.OUTRO
            }
            if (ShowOverlayActionHelper.isLingeringInOutroAnimation(
                    timeSystem,
                    currentTime,
                    actionObject
                )
            ) {
                return HideOverlayAct.LINGERING_OUTRO
            }

            return HideOverlayAct.DO_NOTHING
        }
    }
}