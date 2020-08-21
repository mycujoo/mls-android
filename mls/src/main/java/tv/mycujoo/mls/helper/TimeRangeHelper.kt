package tv.mycujoo.mls.helper

class TimeRangeHelper {
    companion object {
        fun isInRange(currentTime: Long, videoDuration: Long, subjectTime: Long, seekOffset: Long): Boolean {

            var range = 5000L

            val extra20MinSegments = videoDuration / 200000L
            if (extra20MinSegments > 0L) {
                range += extra20MinSegments.toInt() * 1000L
            }

            val right = subjectTime + range + seekOffset
            val left: Long = if (subjectTime + seekOffset < range) {
                0L
            } else {
                subjectTime - range + seekOffset
            }

            return (currentTime >= left) && (currentTime <= right)
        }

        fun isOffsetUntilNow(currentTime: Long, offset: Long): Boolean {
            return currentTime >= offset
        }

    }

}