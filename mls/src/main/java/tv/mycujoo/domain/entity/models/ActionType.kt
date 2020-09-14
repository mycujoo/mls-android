package tv.mycujoo.domain.entity.models

enum class ActionType(val type: String) {
    UNKNOWN("unknown"),
    DELETE_ACTION("delete_action"),
    SHOW_OVERLAY("show_overlay"),
    HIDE_OVERLAY("hide_overlay"),
    SHOW_TIMELINE_MARKER("show_timeline_marker"),
    SET_VARIABLE("set_variable"),
    INCREMENT_VARIABLE("increment_variable"),
    CREATE_TIMER("create_timer"),
    START_TIMER("start_timer"),
    PAUSE_TIMER("pause_timer"),
    ADJUST_TIMER("adjust_timer"),
    SKIP_TIMER("skip_timer");


    companion object {
        fun fromValueOrUnknown(value: String) = values().firstOrNull { it.type == value } ?: UNKNOWN
    }


}
