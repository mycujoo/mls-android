package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.domain.entity.HideOverlayAct
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.mls.enum.C

class HideOverlayActionHelper {
    companion object {
        fun getOverlayActionCurrentAct(
            currentTime: Long,
            actionObject: ActionObject,
            interrupted: Boolean
        ): HideOverlayAct {
            if (actionObject.type != ActionType.HIDE_OVERLAY) {
                throw IllegalArgumentException("Action type should be HIDE_OVERLAY!")
            }
            if (interrupted.not()) {
                if (outroIsInCurrentTimeRange(currentTime, actionObject)) {
                    return HideOverlayAct.OUTRO_IN_RANGE
                }
            } else {
                if (outroIsInCurrentTimeRange(currentTime, actionObject)) {
                    return HideOverlayAct.OUTRO_IN_RANGE
                }
                if (isHideOverlayActionOutroLingering(
                        currentTime,
                        actionObject
                    )
                ) {
                    return HideOverlayAct.OUTRO_LINGERING
                }
                if (isHideOverlayActionOutroLeftover(currentTime, actionObject)) {
                    return HideOverlayAct.OUTRO_LEFTOVER
                }
            }

            return HideOverlayAct.DO_NOTHING
        }

        private fun isHideOverlayActionOutroLingering(
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            if (actionObject.showOverlayRelatedData == null) {
                return false
            }
            if (actionObject.showOverlayRelatedData.outroAnimationDuration == -1L) {
                return false
            }

            if (actionObject.offset > currentTime) {
                return false
            }

            val leftBound = actionObject.offset
            val rightBound =
                actionObject.offset + actionObject.showOverlayRelatedData.outroAnimationDuration

            return (leftBound <= currentTime) && (currentTime < rightBound)
        }

        private fun isHideOverlayActionOutroLeftover(
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            val outroOffset =
                actionObject.offset
            return outroOffset <= currentTime
        }

        private fun outroIsInCurrentTimeRange(
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            val outroOffset =
                actionObject.offset

            return (outroOffset >= currentTime) && (outroOffset < currentTime + C.ONE_SECOND_IN_MS)
        }
    }
}