package tv.mycujoo.mls.utils

class TimeUtils {
    companion object {
        fun convertRelativeTimeToAbsolute(
            windowAbsoluteStartTime: Long,
            actionAbsTime: Long
        ): Long {
            if (actionAbsTime >= windowAbsoluteStartTime) {
                return actionAbsTime - windowAbsoluteStartTime
            } else return -1L
        }
    }
}