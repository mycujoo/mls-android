package tv.mycujoo.domain.entity

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

sealed class Variable {
    abstract val name: String
    abstract fun printValue(): String
    abstract fun increment(amount: Any)
    abstract fun copy(): Variable

    data class LongVariable(override val name: String, var value: Long) : Variable() {
        override fun printValue(): String {
            return value.toString()
        }

        override fun increment(amount: Any) {
            when (amount) {
                is Long -> {
                    value += amount
                }
                is Double -> {
                    value += amount.toLong()
                }
                else -> {
                    // should not happen
                }
            }
        }

        override fun copy(): LongVariable {
            return LongVariable(name, value)
        }

    }

    data class DoubleVariable(
        override val name: String,
        var value: Double,
        val doublePrecision: Int? = null
    ) : Variable() {
        override fun printValue(): String {
            return if (doublePrecision != null) {
                val decimalFormat = DecimalFormat()
                decimalFormat.minimumFractionDigits = doublePrecision
                decimalFormat.maximumFractionDigits = doublePrecision
                val decimalFormatSymbols = DecimalFormatSymbols(Locale.ENGLISH).apply {
                    decimalSeparator = '.'
                }
                decimalFormat.decimalFormatSymbols = decimalFormatSymbols
                decimalFormat.isGroupingUsed = false
                decimalFormat.format(value)

            } else {
                value.toString()
            }
        }

        override fun increment(amount: Any) {
            when (amount) {
                is Long -> {
                    value += amount.toDouble()
                }
                is Double -> {
                    value += amount
                }
                else -> {
                    // should not happen
                }
            }
        }

        override fun copy(): DoubleVariable {
            return DoubleVariable(name, value, doublePrecision)
        }
    }

    data class StringVariable(override val name: String, var value: String) : Variable() {
        override fun printValue(): String {
            return value
        }

        override fun increment(amount: Any) {
            when (amount) {
                is String -> {
                    value = amount
                }
                else -> {
                    // should not happen
                }
            }
        }

        override fun copy(): Variable {
            return StringVariable(name, value)
        }
    }

    data class InvalidVariable(override val name: String = "") : Variable() {
        override fun printValue(): String {
            return ""
        }

        override fun increment(amount: Any) {
            // do nothing
        }

        override fun copy(): InvalidVariable {
            return InvalidVariable(name)
        }
    }

}
