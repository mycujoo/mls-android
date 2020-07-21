package tv.mycujoo.domain.entity.models

enum class ActionType(val type: String) {
    UNKNOWN("unknown"),
    SHOW_OVERLAY("show_overlay"),
    HIDE_OVERLAY("hide_overlay"),
    SHOW_TIMELINE_MARKER("show_timeline_marker"),
    SET_VARIABLE("set_variable"),
    INCREMENT_VARIABLE("increment_variable");


    companion object {
        fun fromValueOrUnknown(value: String) = values().firstOrNull { it.type == value } ?: UNKNOWN
    }


}
