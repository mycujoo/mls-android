package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayAct
import tv.mycujoo.mls.enum.C.Companion.ONE_SECOND_IN_MS

class ShowOverlayActionHelper {
    companion object {
        private fun hasNotReached(currentTime: Long, actionObject: ActionObject): Boolean {
            return (actionObject.offset > currentTime) && (actionObject.offset + ONE_SECOND_IN_MS > currentTime)
        }

        private fun hasPassedDuration(currentTime: Long, actionObject: ActionObject): Boolean {
            return if (actionObject.overlayRelatedData?.duration != -1L) {
                if (actionObject.overlayRelatedData?.outroAnimationDuration != -1L) {
                    (actionObject.offset + actionObject.overlayRelatedData?.duration!! + actionObject.overlayRelatedData.outroAnimationDuration < currentTime)
                } else {
                    (actionObject.offset + actionObject.overlayRelatedData.duration < currentTime)
                }
            } else {
                false
            }

        }

        fun getOverlayActionCurrentAct(
            currentTime: Long,
            actionObject: ActionObject,
            interrupted: Boolean
        ): OverlayAct {
            if (interrupted.not()) {
                if (introIsInCurrentTimeRange(currentTime, actionObject)) {
                    return OverlayAct.INTRO
                }
                if (outroIsInCurrentTimeRange(currentTime, actionObject)) {
                    return OverlayAct.OUTRO
                }
                if (hasPassedDuration(currentTime, actionObject) || hasNotReached(
                        currentTime,
                        actionObject
                    )
                ) {
                    return OverlayAct.REMOVE
                }
                return OverlayAct.DO_NOTHING
            }

            if (isLingeringInIntroAnimation(currentTime, actionObject)) {
                return OverlayAct.LINGERING_INTRO
            }
            if (isLingeringInOutroAnimation(currentTime, actionObject)) {
                return OverlayAct.LINGERING_OUTRO
            }
            if (isLingeringInMidway(currentTime, actionObject)) {
                return OverlayAct.LINGERING_MIDWAY
            }
            if (hasPassedDuration(currentTime, actionObject) || hasNotReached(
                    currentTime,
                    actionObject
                )
            ) {
                return OverlayAct.LINGERING_REMOVE
            }

            return OverlayAct.DO_NOTHING


        }

        private fun introIsInCurrentTimeRange(
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            return (actionObject.offset >= currentTime) && (actionObject.offset < currentTime + ONE_SECOND_IN_MS)
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
            return (outroOffset >= currentTime) && (outroOffset < currentTime + ONE_SECOND_IN_MS)
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
                if (actionObject.overlayRelatedData.duration != -1L) {
                    return false
                }

                // there is no outro specified at all
                if (actionObject.overlayRelatedData.outroAnimationType == AnimationType.UNSPECIFIED || actionObject.overlayRelatedData.outroAnimationDuration == -1L) {
                    return if (AnimationClassifierHelper.hasOutroAnimation(actionObject.overlayRelatedData.outroAnimationType)) {
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
                if (actionObject.overlayRelatedData.duration == -1L) {
                    return false
                }

                if (actionObject.offset == -1L) {
                    return false
                }

                var leftBound = actionObject.offset
                var rightBound = 0L

                if (AnimationClassifierHelper.hasIntroAnimation(actionObject.overlayRelatedData.introAnimationType)) {
                    leftBound =
                        actionObject.offset + actionObject.overlayRelatedData.introAnimationDuration
                }

                rightBound =
                    actionObject.offset + actionObject.overlayRelatedData.duration

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

            val leftBound = actionObject.offset + actionObject.overlayRelatedData.duration
            var rightBound =
                actionObject.offset + actionObject.overlayRelatedData.duration + actionObject.overlayRelatedData.outroAnimationDuration

            return (leftBound <= currentTime) && (currentTime < rightBound)
        }


    }
}