package tv.mycujoo.mls.helper

class TimeRangeHelper {
    companion object {
        fun isInRange(currentTime: Long, subjectTime: Long): Boolean {
            return (subjectTime >= currentTime) && (subjectTime - 15000L <= currentTime)

        }
    }

}