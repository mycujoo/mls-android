package tv.mycujoo.mcls.helper

class ViewersCounterHelper {
    companion object {
        /**
         * Validates a viewer count number.
         * @return true if value is valid, false other wise.
         */
        fun isViewersCountValid(count: String?): Boolean {
            if (count.isNullOrEmpty()) {
                return false
            }

            return try {
                val countInt = count.toInt()
                countInt > 1
            } catch (e: Exception) {
                false
            }
        }
    }

}