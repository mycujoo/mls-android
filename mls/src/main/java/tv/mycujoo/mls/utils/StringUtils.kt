package tv.mycujoo.mls.utils

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
    }
}