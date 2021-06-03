package tv.mycujoo.domain.entity.models

/**
 * Types of Annotation Action
 */
enum class ActionType(val type: String) {
    /**
     * overlay related
     */
    SHOW_OVERLAY("show_overlay"),
    HIDE_OVERLAY("hide_overlay"),
    RESHOW_OVERLAY("reshow_overlay"),

    /**
     * timer related
     */
    CREATE_TIMER("create_timer"),
    START_TIMER("start_timer"),
    PAUSE_TIMER("pause_timer"),
    ADJUST_TIMER("adjust_timer"),
    SKIP_TIMER("skip_timer"),

    /**
     * variable related
     */
    SET_VARIABLE("set_variable"),
    INCREMENT_VARIABLE("increment_variable"),

    /**
     * timeline-marker related
     */
    SHOW_TIMELINE_MARKER("show_timeline_marker"),

    /**
     * msc
     */
    DELETE_ACTION("delete_action"),
    UNKNOWN("unknown");


    companion object {
        fun fromValueOrUnknown(value: String) = values().firstOrNull { it.type == value } ?: UNKNOWN
    }


}
