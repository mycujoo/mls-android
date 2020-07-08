package tv.mycujoo.domain.entity.models

enum class ActionType(val type: String) {
    UNKNOWN("unknown"),
    SHOW_OVERLAY("show_overlay"),
    HIDE_OVERLAY("hide_overlay"),
    SHOW_TIMELINE_MARKER("show_timeline_marker");


    companion object {
        fun fromValueOrUnknown(value: String) = values().firstOrNull { it.type == value } ?: UNKNOWN
    }


}
