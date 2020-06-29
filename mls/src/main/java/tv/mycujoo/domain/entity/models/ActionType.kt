package tv.mycujoo.domain.entity.models

enum class ActionType(val type: String) {
    UNKNOWN("unknown"),
    SHOW_OVERLAY("show_overlay"),
    HIDE_OVERLAY("hide_overlay");

    companion object {
        fun fromValueOrUnknown(value: String) = values().firstOrNull { it.type == value } ?: UNKNOWN
    }


}
