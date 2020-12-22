package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.HideOverlayAct
import tv.mycujoo.mls.enum.C

class HideOverlayActionHelper {
    companion object {
        fun getOverlayActionCurrentAct(
            currentTime: Long,
            hideOverlayAction: Action.HideOverlayAction,
            interrupted: Boolean
        ): HideOverlayAct {
            if (interrupted.not()) {
                if (outroIsInCurrentTimeRange(currentTime, hideOverlayAction)) {
                    return HideOverlayAct.OUTRO_IN_RANGE
                }
            } else {
                if (outroIsInCurrentTimeRange(currentTime, hideOverlayAction)) {
                    return HideOverlayAct.OUTRO_IN_RANGE
                }
                if (isHideOverlayActionOutroLingering(
                        currentTime,
                        hideOverlayAction
                    )
                ) {
                    return HideOverlayAct.OUTRO_LINGERING
                }
                if (isHideOverlayActionOutroLeftover(currentTime, hideOverlayAction)) {
                    return HideOverlayAct.OUTRO_LEFTOVER
                }
            }

            return HideOverlayAct.DO_NOTHING
        }

        private fun isHideOverlayActionOutroLingering(
            currentTime: Long,
            hideOverlayAction: Action.HideOverlayAction
        ): Boolean {

            if (hideOverlayAction.outroAnimationSpec == null) {
                return false
            }

            if (hideOverlayAction.offset > currentTime) {
                return false
            }

            val leftBound = hideOverlayAction.offset
            val rightBound =
                hideOverlayAction.offset + hideOverlayAction.outroAnimationSpec.animationDuration

            return (leftBound <= currentTime) && (currentTime < rightBound)
        }

        private fun isHideOverlayActionOutroLeftover(
            currentTime: Long,
            hideOverlayAction: Action.HideOverlayAction
        ): Boolean {
            val outroOffset =
                hideOverlayAction.offset
            return outroOffset <= currentTime
        }

        private fun outroIsInCurrentTimeRange(
            currentTime: Long,
            hideOverlayAction: Action.HideOverlayAction
        ): Boolean {
            val outroOffset =
                hideOverlayAction.offset

            return (outroOffset >= currentTime) && (outroOffset < currentTime + C.ONE_SECOND_IN_MS)
        }
    }
}