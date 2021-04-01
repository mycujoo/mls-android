package tv.mycujoo.mcls.utils

import tv.mycujoo.mcls.extensions.length

class MathUtils {
    companion object {
        /**
         * Convert long to int safely. Similar with Math.toIntExact() in Java 8.
         *
         * @param numLong Number of type long to convert.
         * @return int version of input.
         * @throws ArithmeticException If input overflows int.
         */
        fun safeLongToInt(numLong: Long): Int {
            if (numLong.toInt().toLong() != numLong) {
                throw ArithmeticException("Input overflows int.\n")
            }

            return numLong.toInt()
        }

        /**
         * Convert to epoch timestamp in Milli-seconds
         *
         * @param epoch epoch in second or milli-second format
         * @return epoch in second format
         */
        fun convertToEpochInMS(epoch: Long): Long {
            return if (epoch.length() < 12) {
                epoch * 1000L
            } else epoch
        }
    }
}