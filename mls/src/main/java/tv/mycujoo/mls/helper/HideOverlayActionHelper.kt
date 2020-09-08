package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.domain.entity.OverlayAct

class HideOverlayActionHelper {
    companion object {
        fun getOverlayActionCurrentAct(
            currentTime: Long,
            actionObject: ActionObject
        ): OverlayAct {
            if (ShowOverlayActionHelper.outroIsInCurrentTimeRange(currentTime, actionObject)) {
                return OverlayAct.OUTRO
            }

            return OverlayAct.DO_NOTHING
        }
    }
}