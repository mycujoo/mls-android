package tv.mycujoo.mls.utils

import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class StringUtils {
    companion object {
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
         * Reads Segment url name and returns [String] presentation of absolute time value or "-1".
         *
         * @param name Segment url name [String] which should be read.
         * @return Returns `-1` if the the segment name is not in correct format. Else
         * returns the absolute time from segment url name as [String].
         */
        fun getSegmentTimeStamp(name: String): String {
            val keyword = "segment_"
            val underscore = "_"
            return if (name.contains(keyword, true)) {
                name.substringAfter(keyword).substringBefore(underscore)
            } else
                "-1"
        }
    }
}