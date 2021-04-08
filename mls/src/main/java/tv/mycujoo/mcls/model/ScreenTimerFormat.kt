package tv.mycujoo.mcls.model

enum class ScreenTimerFormat(var type: String) {
    MINUTES_SECONDS("ms"),
    SECONDS("s"),
    UNKNOWN("unknown");

    companion object {
        fun fromValueOrUnknown(value: String) =
            values().firstOrNull { it.type == value } ?: UNKNOWN
    }
}
