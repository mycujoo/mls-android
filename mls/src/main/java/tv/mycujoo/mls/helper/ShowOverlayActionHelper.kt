package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.OverlayAct
import tv.mycujoo.mls.enum.C.Companion.ONE_SECOND_IN_MS

class ShowOverlayActionHelper {
    companion object {
        private fun hasNotReached(
            currentTime: Long,
            actionObject: Action.ShowOverlayAction
        ): Boolean {
            return (actionObject.offset > currentTime) && (actionObject.offset + ONE_SECOND_IN_MS > currentTime)
        }

        private fun hasPassedDuration(
            currentTime: Long,
            action: Action.ShowOverlayAction
        ): Boolean {
            return if (action.duration != null) {
                if (action.outroTransitionSpec != null && action.outroTransitionSpec.animationDuration != -1L) {
                    (action.offset + action.duration + action.outroTransitionSpec.animationDuration + ONE_SECOND_IN_MS < currentTime)
                } else {
                    (action.offset + action.duration < currentTime)
                }
            } else {
                false
            }
        }

        fun getOverlayActionCurrentAct(
            currentTime: Long,
            action: Action.ShowOverlayAction,
            interrupted: Boolean
        ): OverlayAct {
            if (interrupted.not()) {
                if (introIsInCurrentTimeRange(currentTime, action)) {
                    return OverlayAct.INTRO
                }
                if (outroIsInCurrentTimeRange(currentTime, action)) {
                    return OverlayAct.OUTRO
                }
                if (hasPassedDuration(currentTime, action) ||
                    hasNotReached(currentTime, action)
                ) {
                    return OverlayAct.REMOVE
                }
                return OverlayAct.DO_NOTHING
            }

            if (isLingeringInIntroAnimation(currentTime, action)) {
                return OverlayAct.LINGERING_INTRO
            }
            if (isLingeringInOutroAnimation(currentTime, action)) {
                return OverlayAct.LINGERING_OUTRO
            }
            if (isLingeringInMidway(currentTime, action)) {
                return OverlayAct.LINGERING_MIDWAY
            }
            if (hasPassedDuration(currentTime, action) || hasNotReached(
                    currentTime,
                    action
                )
            ) {
                return OverlayAct.LINGERING_REMOVE
            }

            return OverlayAct.DO_NOTHING
        }

        private fun introIsInCurrentTimeRange(
            currentTime: Long,
            action: Action
        ): Boolean {
            return (action.offset >= currentTime) && (action.offset < currentTime + ONE_SECOND_IN_MS)
        }


        private fun outroIsInCurrentTimeRange(
            currentTime: Long,
            action: Action.ShowOverlayAction
        ): Boolean {
            if (action.duration == null) {
                return false
            }

            val outroOffset = action.offset + action.duration
            return (outroOffset >= currentTime) && (outroOffset < currentTime + ONE_SECOND_IN_MS)
        }


        private fun isLingeringInIntroAnimation(
            currentTime: Long,
            action: Action.ShowOverlayAction
        ): Boolean {
            if (action.offset > currentTime) {
                return false
            }

            if (action.introTransitionSpec == null || action.introTransitionSpec.animationDuration <= 0L) {
                return false
            }

            val leftBound = action.offset
            val rightBound =
                action.offset + action.introTransitionSpec.animationDuration

            return (leftBound <= currentTime) && (currentTime < rightBound)
        }


        private fun isLingeringInMidway(
            currentTime: Long,
            action: Action.ShowOverlayAction
        ): Boolean {
            fun isLingeringUnbounded(
                currentTime: Long,
                action: Action.ShowOverlayAction
            ): Boolean {
                if (action.offset > currentTime) {
                    return false
                }
                if (action.duration == null) {
                    return false
                }

                if (action.outroTransitionSpec != null) {
                    return if (AnimationClassifierHelper.hasOutroAnimation(action.outroTransitionSpec.animationType)) {
                        currentTime > action.offset + action.outroTransitionSpec.animationDuration
                    } else {
                        currentTime > action.offset
                    }
                }
                return false
            }


            fun isLingeringBounded(
                currentTime: Long,
                action: Action.ShowOverlayAction
            ): Boolean {
                if (action.duration == null) {
                    return false
                }

                if (action.offset > currentTime) {
                    return false
                }

                var leftBound = action.offset
                var rightBound = 0L

                if (action.introTransitionSpec != null &&
                    AnimationClassifierHelper.hasIntroAnimation(action.introTransitionSpec.animationType)
                ) {
                    leftBound =
                        action.offset + action.introTransitionSpec.animationDuration
                }

                rightBound =
                    action.offset + action.duration

                return (currentTime > leftBound) && (currentTime < rightBound)

            }



            return (isLingeringUnbounded(currentTime, action) ||
                    isLingeringBounded(currentTime, action))
        }

        private fun isLingeringInOutroAnimation(
            currentTime: Long,
            action: Action.ShowOverlayAction
        ): Boolean {
            if (action.duration == null) {
                return false
            }
            if (action.outroTransitionSpec == null) {
                return false
            }
            if (action.outroTransitionSpec.animationDuration == -1L) {
                return false
            }

            if (action.offset > currentTime) {
                return false
            }

            val leftBound = action.offset + action.duration
            val rightBound =
                action.offset + action.duration + action.outroTransitionSpec.animationDuration

            return (leftBound <= currentTime) && (currentTime < rightBound)
        }


    }
}