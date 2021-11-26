package tv.mycujoo.domain.entity

import java.util.*

/**
 * Define type of a Variable
 */
enum class VariableType {
    UNSPECIFIED,
    DOUBLE,
    LONG,
    STRING;

    companion object {
        fun fromValueOrNone(value: String) =
            values().firstOrNull { it.name.lowercase(Locale.getDefault()) == value }
                ?: UNSPECIFIED
    }
}