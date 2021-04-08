package tv.mycujoo.mcls.helper

class TimeRangeHelper {
    companion object {
        fun isInRange(currentPositionOnScreen: Float, poiPositionsOnScreen: Int): Boolean {
            return poiPositionsOnScreen != -1 && currentPositionOnScreen.toInt() in poiPositionsOnScreen - 10..poiPositionsOnScreen + 10
        }

        fun isInRangeTV(currentPositionOnScreen: Float, poiPositionsOnScreen: Int): Boolean {
            return poiPositionsOnScreen != -1 && currentPositionOnScreen.toInt() in poiPositionsOnScreen - 5..poiPositionsOnScreen + 5
        }

        fun isOffsetUntilNow(currentTime: Long, offset: Long): Boolean {
            return currentTime >= offset
        }

        fun isCurrentTimeInDvrWindowDuration(duration: Long, dvrWindowDuration: Long): Boolean {
            return duration + 20000L <= dvrWindowDuration
        }

    }

}