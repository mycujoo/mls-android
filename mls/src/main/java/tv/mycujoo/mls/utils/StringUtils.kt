package tv.mycujoo.mls.utils

import com.google.android.exoplayer2.C
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class StringUtils {
    companion object {
        /**
         * Returns the specified millisecond time formatted as a string.
         *
         * @param timeMs The time to format as a string, in milliseconds.
         * @return The time formatted as a string.
         */
        fun getFormattedTime(
            timeMs: Long,
            stringBuilder: StringBuilder,
            timeFormatter: Formatter
        ): String? {
            var timeMs = timeMs
            if (timeMs == C.TIME_UNSET) {
                timeMs = 0
            }
            val totalSeconds = timeMs / 1000
            val seconds = totalSeconds % 60
            val minutes = totalSeconds / 60 % 60
            val hours = totalSeconds / 3600
            stringBuilder.setLength(0)
            return if (hours > 0) timeFormatter.format("%d:%02d:%02d", hours, minutes, seconds)
                .toString() else timeFormatter.format("%02d:%02d", minutes, seconds).toString()
        }

        fun getNumberOfViewers(count: String?): String {
            if (count.isNullOrEmpty()) {
                return "0"
            }

            val number: Int
            try {
                number = count.toInt()
            } catch (e: Exception) {
                return "0"
            }

            return when {
                number < 1 -> "0"
                number < 1000 -> number.toString()
                number < 1000000 -> {
                    val decimalFormat = DecimalFormat()
                    decimalFormat.maximumFractionDigits = 1
                    val decimalFormatSymbols = DecimalFormatSymbols(Locale.ENGLISH).apply {
                        decimalSeparator = '.'
                        groupingSeparator = '.'
                    }
                    decimalFormat.decimalFormatSymbols = decimalFormatSymbols
                    decimalFormat.format(number / 1000f) + "K"
                }
                else -> {
                    val decimalFormat = DecimalFormat()
                    decimalFormat.maximumFractionDigits = 1
                    val decimalFormatSymbols = DecimalFormatSymbols(Locale.ENGLISH).apply {
                        decimalSeparator = '.'
                        groupingSeparator = '.'
                    }
                    decimalFormat.decimalFormatSymbols = decimalFormatSymbols
                    decimalFormat.format(number / 1000000f) + "M"
                }
            }
        }

        /**
         * Will read the content from a given [InputStream] and return it as a [String].
         *
         * @param inputStream The [InputStream] which should be read.
         * @return Returns `null` if the the [InputStream] could not be read. Else
         * returns the content of the [InputStream] as [String].
         */
        fun inputStreamToString(inputStream: InputStream): String? {
            return try {
                val bytes = ByteArray(inputStream.available())
                inputStream.read(bytes, 0, bytes.size)
                String(bytes)
            } catch (e: IOException) {
                null
            }
        }

        /**
         * Reads Segment url name and returns [Long] presentation of absolute time value or "-1".
         *
         * @param name Segment url name [String] which should be read.
         * @return Returns `-1` if the the segment name is not in correct format. Else
         * returns the absolute time from segment url name as [Long].
         */
        fun getSegmentTimeStamp(name: String): Long {
            val keyword = "segment_"
            val rightBound = "_"
            if (name.contains(keyword, true) && name.contains(rightBound, true)) {
                val timeString = name.substringAfter(keyword).substringBefore(rightBound)

                return try {
                    timeString.toLong()
                } catch (e: Exception) {
                    println(e)
                    -1L
                }

            } else
                return -1
        }
    }
}