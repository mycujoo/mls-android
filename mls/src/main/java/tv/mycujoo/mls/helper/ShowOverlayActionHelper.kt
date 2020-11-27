package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayAct
import tv.mycujoo.mls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mls.helper.TimeSystem.ABSOLUTE
import tv.mycujoo.mls.helper.TimeSystem.RELATIVE

class ShowOverlayActionHelper {
    companion object {
        private fun hasNotReached(
            timeSystem: TimeSystem,
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            return when (timeSystem) {
                RELATIVE -> {
                    (actionObject.offset > currentTime) && (actionObject.offset + ONE_SECOND_IN_MS > currentTime)
                }
                ABSOLUTE -> {
                    (actionObject.absoluteTime > currentTime) && (actionObject.absoluteTime + ONE_SECOND_IN_MS > currentTime)
                }
            }

        }

        private fun hasPassedDuration(
            timeSystem: TimeSystem,
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            when (timeSystem) {
                RELATIVE -> {
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
                ABSOLUTE -> {
                    return if (actionObject.overlayRelatedData?.duration != -1L) {
                        if (actionObject.overlayRelatedData?.outroAnimationDuration != -1L) {
                            (actionObject.absoluteTime + actionObject.overlayRelatedData?.duration!! + actionObject.overlayRelatedData.outroAnimationDuration < currentTime)
                        } else {
                            (actionObject.absoluteTime + actionObject.overlayRelatedData.duration < currentTime)
                        }
                    } else {
                        false
                    }
                }
            }
        }

        fun getOverlayActionCurrentAct(
            timeSystem: TimeSystem,
            currentTime: Long,
            actionObject: ActionObject,
            interrupted: Boolean
        ): OverlayAct {
            if (interrupted.not()) {
                if (introIsInCurrentTimeRange(timeSystem, currentTime, actionObject)) {
                    return OverlayAct.INTRO
                }
                if (outroIsInCurrentTimeRange(timeSystem, currentTime, actionObject)) {
                    return OverlayAct.OUTRO
                }
                if (hasPassedDuration(timeSystem, currentTime, actionObject) || hasNotReached(
                        timeSystem,
                        currentTime,
                        actionObject
                    )
                ) {
                    return OverlayAct.REMOVE
                }
                return OverlayAct.DO_NOTHING
            }

            if (isLingeringInIntroAnimation(timeSystem, currentTime, actionObject)) {
                return OverlayAct.LINGERING_INTRO
            }
            if (isLingeringInOutroAnimation(timeSystem, currentTime, actionObject)) {
                return OverlayAct.LINGERING_OUTRO
            }
            if (isLingeringInMidway(timeSystem, currentTime, actionObject)) {
                return OverlayAct.LINGERING_MIDWAY
            }
            if (hasPassedDuration(timeSystem, currentTime, actionObject) || hasNotReached(
                    timeSystem,
                    currentTime,
                    actionObject
                )
            ) {
                return OverlayAct.LINGERING_REMOVE
            }

            return OverlayAct.DO_NOTHING
        }

        private fun introIsInCurrentTimeRange(
            timeSystem: TimeSystem,
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            return when (timeSystem) {
                RELATIVE -> {
                    (actionObject.offset >= currentTime) && (actionObject.offset < currentTime + ONE_SECOND_IN_MS)
                }
                ABSOLUTE -> {
                    (actionObject.absoluteTime >= currentTime) && (actionObject.absoluteTime < currentTime + ONE_SECOND_IN_MS)
                }
            }

        }

        fun outroIsInCurrentTimeRange(
            timeSystem: TimeSystem,
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            // there is no outro specified at all, or there is no duration specified
            if (actionObject.overlayRelatedData!!.outroAnimationType == AnimationType.UNSPECIFIED ||
                actionObject.overlayRelatedData.duration < 0L
            ) {
                return false
            }
            return when (timeSystem) {
                RELATIVE -> {
                    val outroOffset = actionObject.offset + actionObject.overlayRelatedData.duration
                    (outroOffset >= currentTime) && (outroOffset < currentTime + ONE_SECOND_IN_MS)
                }
                ABSOLUTE -> {
                    val outroOffset =
                        actionObject.absoluteTime + actionObject.overlayRelatedData.duration
                    (outroOffset >= currentTime) && (outroOffset < currentTime + ONE_SECOND_IN_MS)
                }
            }

        }

        private fun isLingeringInIntroAnimation(
            timeSystem: TimeSystem,
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            when (timeSystem) {
                RELATIVE -> {
                    if (actionObject.offset > currentTime) {
                        return false
                    }

                    val leftBound = actionObject.offset
                    val rightBound =
                        actionObject.offset + actionObject.overlayRelatedData!!.introAnimationDuration

                    return (leftBound <= currentTime) && (currentTime < rightBound)
                }
                ABSOLUTE -> {
                    if (actionObject.absoluteTime > currentTime) {
                        return false
                    }

                    val leftBound = actionObject.absoluteTime
                    val rightBound =
                        actionObject.absoluteTime + actionObject.overlayRelatedData!!.introAnimationDuration

                    return (leftBound <= currentTime) && (currentTime < rightBound)
                }
            }

        }

        private fun isLingeringInMidway(
            timeSystem: TimeSystem,
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            fun isLingeringUnbounded(
                timeSystem: TimeSystem,
                currentTime: Long,
                actionObject: ActionObject
            ): Boolean {
                if (actionObject.overlayRelatedData == null) {
                    return false
                }
                if (actionObject.overlayRelatedData.duration != -1L) {
                    return false
                }
                when (timeSystem) {
                    RELATIVE -> {
                        if (actionObject.offset > currentTime) {
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
                    ABSOLUTE -> {
                        if (actionObject.absoluteTime > currentTime) {
                            return false
                        }

                        // there is no outro specified at all
                        if (actionObject.overlayRelatedData.outroAnimationType == AnimationType.UNSPECIFIED || actionObject.overlayRelatedData.outroAnimationDuration == -1L) {
                            return if (AnimationClassifierHelper.hasOutroAnimation(actionObject.overlayRelatedData.outroAnimationType)) {
                                currentTime > actionObject.absoluteTime + actionObject.overlayRelatedData.outroAnimationDuration
                            } else {
                                currentTime > actionObject.absoluteTime
                            }
                        }
                        return false
                    }
                }

            }

            fun isLingeringBounded(
                timeSystem: TimeSystem,
                currentTime: Long,
                actionObject: ActionObject
            ): Boolean {
                if (actionObject.overlayRelatedData == null) {
                    return false
                }
                if (actionObject.overlayRelatedData.duration == -1L) {
                    return false
                }
                when (timeSystem) {
                    RELATIVE -> {
                        if (actionObject.offset == -1L) {
                            return false
                        }

                        if (actionObject.offset > currentTime) {
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
                    ABSOLUTE -> {
                        if (actionObject.absoluteTime == -1L) {
                            return false
                        }
                        if (actionObject.absoluteTime > currentTime) {
                            return false
                        }


                        var leftBound = actionObject.absoluteTime
                        var rightBound = 0L

                        if (AnimationClassifierHelper.hasIntroAnimation(actionObject.overlayRelatedData.introAnimationType)) {
                            leftBound =
                                actionObject.absoluteTime + actionObject.overlayRelatedData.introAnimationDuration
                        }

                        rightBound =
                            actionObject.absoluteTime + actionObject.overlayRelatedData.duration

                        return (currentTime > leftBound) && (currentTime < rightBound)
                    }
                }

            }

            return (isLingeringUnbounded(timeSystem, currentTime, actionObject) ||
                    isLingeringBounded(
                        timeSystem, currentTime, actionObject
                    ))
        }

        fun isLingeringInOutroAnimation(
            timeSystem: TimeSystem,
            currentTime: Long,
            actionObject: ActionObject
        ): Boolean {
            if (actionObject.overlayRelatedData == null) {
                return false
            }

            if (actionObject.overlayRelatedData.duration == -1L) {
                return false
            }

            if (actionObject.overlayRelatedData.outroAnimationDuration == -1L) {
                return false
            }
            when (timeSystem) {
                RELATIVE -> {
                    if (actionObject.offset > currentTime) {
                        return false
                    }

                    val leftBound = actionObject.offset + actionObject.overlayRelatedData.duration
                    val rightBound =
                        actionObject.offset + actionObject.overlayRelatedData.duration + actionObject.overlayRelatedData.outroAnimationDuration

                    return (leftBound <= currentTime) && (currentTime < rightBound)
                }
                ABSOLUTE -> {
                    if (actionObject.absoluteTime > currentTime) {
                        return false
                    }

                    val leftBound =
                        actionObject.absoluteTime + actionObject.overlayRelatedData.duration
                    val rightBound =
                        actionObject.absoluteTime + actionObject.overlayRelatedData.duration + actionObject.overlayRelatedData.outroAnimationDuration

                    return (leftBound <= currentTime) && (currentTime < rightBound)
                }
            }

        }


    }
}