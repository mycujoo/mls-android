package tv.mycujoo.domain.entity

data class OverlayEntity(
    var id: String,
    var svgData: SvgData?,
    var viewSpec: ViewSpec,
    var introTransitionSpec: TransitionSpec,
    var outroTransitionSpec: TransitionSpec,
    val variablePlaceHolders: List<String>
) {

    var isDownloading = false
    var isOnScreen = false

    fun update(currentTime: Long): OverlayAct {
        if (isDownloading) {
            return OverlayAct.DO_NOTHING
        }

        if (!isOnScreen && introIsInCurrentTimeRange(currentTime)) {
            return OverlayAct.INTRO
        }

        if (isOnScreen && outroIsInCurrentTimeRange(currentTime)) {
            return OverlayAct.OUTRO
        }



        return OverlayAct.DO_NOTHING
    }

    fun forceUpdate(currentTime: Long): OverlayAct {
        if (isLingeringInIntroAnimation(currentTime)) {
            return OverlayAct.LINGERING_INTRO
        }

        if (isLingeringInMidway(currentTime)) {
            return OverlayAct.LINGERING_MIDWAY
        }

        if (isLingeringInOutroAnimation(currentTime) && hasOutroAnimation(outroTransitionSpec.animationType)) {
            return OverlayAct.LINGERING_OUTRO
        }

        return OverlayAct.LINGERING_REMOVE
    }


    private fun introIsInCurrentTimeRange(
        currentTime: Long
    ): Boolean {
        return (introTransitionSpec.offset >= currentTime) && (introTransitionSpec.offset < currentTime + 1000L)
    }

    private fun outroIsInCurrentTimeRange(
        currentTime: Long
    ): Boolean {
        // there is no outro specified at all
        if (outroTransitionSpec.animationType == AnimationType.UNSPECIFIED) {
            return false
        }

        return (outroTransitionSpec.offset >= currentTime) && (outroTransitionSpec.offset < currentTime + 1000L)
    }

    private fun isLingeringInIntroAnimation(
        currentTime: Long
    ): Boolean {
        if (introTransitionSpec.offset > currentTime) {
            return false
        }

        val leftBound = introTransitionSpec.offset
        val rightBound =
            introTransitionSpec.offset + introTransitionSpec.animationDuration

        return (leftBound <= currentTime) && (currentTime < rightBound)
    }

    private fun isLingeringInMidway(currentTime: Long): Boolean {
        fun isLingeringUnbounded(currentTime: Long): Boolean {
            if (introTransitionSpec.offset > currentTime) {
                return false
            }

            // there is no outro specified at all
            if (outroTransitionSpec.animationType == AnimationType.UNSPECIFIED || outroTransitionSpec.animationDuration == -1L) {
                return if (hasEnteringAnimation(introTransitionSpec.animationType)) {
                    currentTime > introTransitionSpec.offset + introTransitionSpec.animationDuration
                } else {
                    currentTime > introTransitionSpec.offset
                }
            }
            return false
        }

        fun isLingeringBounded(currentTime: Long): Boolean {
            if (introTransitionSpec.offset > currentTime) {
                return false
            }

            if (outroTransitionSpec.offset == -1L || outroTransitionSpec.animationDuration == 0L) {
                return false
            }

            var leftBound = introTransitionSpec.offset
            var rightBound = 0L

            if (hasEnteringAnimation(introTransitionSpec.animationType)) {
                leftBound =
                    introTransitionSpec.offset + introTransitionSpec.animationDuration
            }

            if (hasOutroAnimation(outroTransitionSpec.animationType)) {
                rightBound = outroTransitionSpec.offset
            }

            return (currentTime > leftBound) && (currentTime < rightBound)
        }

        return (isLingeringUnbounded(currentTime) || isLingeringBounded(
            currentTime
        ))
    }


    private fun isLingeringInOutroAnimation(
        currentTime: Long
    ): Boolean {
        if (introTransitionSpec.offset > currentTime) {
            return false
        }

        if (outroTransitionSpec.animationDuration == -1L || outroTransitionSpec.animationDuration > currentTime) {
            return false
        }

        val leftBound = outroTransitionSpec.offset
        val rightBound =
            outroTransitionSpec.offset + outroTransitionSpec.animationDuration

        return (leftBound <= currentTime) && (currentTime < rightBound)
    }

    private fun hasEnteringAnimation(animationType: AnimationType): Boolean {
        return when (animationType) {
            AnimationType.FADE_IN,
            AnimationType.SLIDE_FROM_LEFT,
            AnimationType.SLIDE_FROM_RIGHT -> {
                true
            }
            else -> false
        }
    }

    private fun hasOutroAnimation(animationType: AnimationType): Boolean {
        return when (animationType) {
            AnimationType.FADE_OUT,
            AnimationType.SLIDE_TO_LEFT,
            AnimationType.SLIDE_TO_RIGHT -> {
                true
            }
            else -> false
        }
    }
}