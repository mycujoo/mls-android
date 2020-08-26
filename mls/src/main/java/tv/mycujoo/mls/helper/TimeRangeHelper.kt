package tv.mycujoo.mls.helper

class TimeRangeHelper {
    companion object {
        fun isInRange(currentPositionOnScreen: Float, poiPositionsOnScreen: Int): Boolean {
            return poiPositionsOnScreen != -1 && currentPositionOnScreen.toInt() in poiPositionsOnScreen - 10..poiPositionsOnScreen + 10
        }

        fun isOffsetUntilNow(currentTime: Long, offset: Long): Boolean {
            return currentTime >= offset
        }

    }

}