package tv.mycujoo.mcls.model

/**
 * Format of Timer, which indicates how a timer is displayed.
 * Possible options are MM:SS and SS
 */
enum class ScreenTimerFormat(var type: String) {
    MINUTES_SECONDS("ms"),
    SECONDS("s"),
    UNKNOWN("unknown");

    companion object {
        fun fromValueOrUnknown(value: String) =
            values().firstOrNull { it.type == value } ?: UNKNOWN
    }
}
