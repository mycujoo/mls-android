package tv.mycujoo.mls.helper

class TimeRangeHelper {
    companion object {
        fun isInRange(currentTime: Long, videoDuration: Long, subjectTime: Long): Boolean {

            var range = 5000L

            val extra20MinSegments = videoDuration / 200000L
            if (extra20MinSegments > 0L) {
                range += extra20MinSegments.toInt() * 1000L
            }

            val right = subjectTime + range
            val left: Long = if (subjectTime < range) {
                0L
            } else {
                subjectTime - range
            }

            return (currentTime >= left) && (currentTime <= right)
        }
    }

}