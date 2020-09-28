package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayAct

class ShowOverlayActionHelper {
    companion object {

        fun getOverlayActionCurrentAct(
            currentTime: Long,
            actionObject: ActionObject,
            interrupted: Boolean
        ): OverlayAct {
            if (introIsInCurrentTimeRange(currentTime, actionObject)) {
                return OverlayAct.INTRO
            }
            if (outroIsInCurrentTimeRange(currentTime, actionObject)) {
                return OverlayAct.OUTRO
            }

            if (interrupted.not()) {
                return OverlayAct.DO_NOTHING
            } else {

                if (actionObject.offset < currentTime && actionObject.offset + actionObject.overlayRelatedData!!.duration > currentTime){
                    return OverlayAct.LINGERING_MIDWAY
                }

                if (isLingeringInIntroAnimation(currentTime, actionObject)) {
                    return OverlayAct.LINGERING_INTRO
                }
                if (isLingeringInMidway(currentTime, actionObject)) {
                    return OverlayAct.LINGERING_MIDWAY
                }
                if (isLingeringInOutroAnimation(currentTime, actionObject)) {
                    return OverlayAct.LINGERING_OUTRO
                } else return OverlayAct.LINGERING_REMOVE
            }


        }

        fun introIsInCurrentTimeRange(
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            return (actionObject.offset >= currentTime) && (actionObject.offset < currentTime + 1000L)
        }

        fun outroIsInCurrentTimeRange(
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            // there is no outro specified at all, or there is no duration specified
            if (actionObject.overlayRelatedData!!.outroAnimationType == AnimationType.UNSPECIFIED ||
                actionObject.overlayRelatedData.duration < 0L
            ) {
                return false
            }
            val outroOffset = actionObject.offset + actionObject.overlayRelatedData.duration
            return (outroOffset >= currentTime) && (outroOffset < currentTime + 1000L)
        }

        private fun isLingeringInIntroAnimation(
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            if (actionObject.offset > currentTime) {
                return false
            }

            val leftBound = actionObject.offset
            val rightBound =
                actionObject.offset + actionObject.overlayRelatedData!!.introAnimationDuration

            return (leftBound <= currentTime) && (currentTime < rightBound)
        }

        private fun isLingeringInMidway(currentTime: Long, actionObject: ActionObject): Boolean {
            fun isLingeringUnbounded(currentTime: Long, actionObject: ActionObject): Boolean {
                if (actionObject.offset > currentTime) {
                    return false
                }
                if (actionObject.overlayRelatedData == null) {
                    return false
                }

                // there is no outro specified at all
                if (actionObject.overlayRelatedData.outroAnimationType == AnimationType.UNSPECIFIED || actionObject.overlayRelatedData.outroAnimationDuration == -1L) {
                    return if (AnimationClassifierHelper.hasIntroAnimation(actionObject.overlayRelatedData.outroAnimationType)) {
                        currentTime > actionObject.offset + actionObject.overlayRelatedData.outroAnimationDuration
                    } else {
                        currentTime > actionObject.offset
                    }
                }
                return false
            }

            fun isLingeringBounded(currentTime: Long, actionObject: ActionObject): Boolean {
                if (actionObject.offset > currentTime) {
                    return false
                }
                if (actionObject.overlayRelatedData == null) {
                    return false
                }

                if (actionObject.offset == -1L || actionObject.overlayRelatedData.outroAnimationDuration == 0L) {
                    return false
                }

                var leftBound = actionObject.offset
                var rightBound = 0L

                if (AnimationClassifierHelper.hasIntroAnimation(actionObject.overlayRelatedData.introAnimationType)) {
                    leftBound =
                        actionObject.offset + actionObject.overlayRelatedData.introAnimationDuration
                }

                if (AnimationClassifierHelper.hasOutroAnimation(actionObject.overlayRelatedData.outroAnimationType)) {
                    rightBound = actionObject.offset
                }

                return (currentTime > leftBound) && (currentTime < rightBound)
            }

            return (isLingeringUnbounded(currentTime, actionObject) || isLingeringBounded(
                currentTime, actionObject
            ))
        }

        private fun isLingeringInOutroAnimation(
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            if (actionObject.offset > currentTime) {
                return false
            }
            if (actionObject.overlayRelatedData == null) {
                return false
            }

            if (actionObject.overlayRelatedData.duration == -1L) {
                return false
            }

            if (actionObject.overlayRelatedData.outroAnimationDuration == -1L) {
                return false
            }

            val leftBound = actionObject.offset
            var rightBound =
                actionObject.offset + actionObject.overlayRelatedData.duration + actionObject.overlayRelatedData.outroAnimationDuration

            return (leftBound <= currentTime) && (currentTime < rightBound)
        }


    }
}