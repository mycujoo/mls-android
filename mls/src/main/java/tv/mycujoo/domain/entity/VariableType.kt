package tv.mycujoo.domain.entity

import java.util.*

enum class VariableType {
    UNSPECIFIED,
    DOUBLE,
    LONG,
    STRING;

    companion object {
        fun fromValueOrNone(value: String) =
            values().firstOrNull { it.name.toLowerCase(Locale.getDefault()) == value }
                ?: UNSPECIFIED
    }
}