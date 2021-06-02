package tv.mycujoo.domain.entity

/**
 * Types of animations for Overlay view.
 */
enum class AnimationType(val type: String) {
    /**
     * No animation
     */
    NONE("none"),

    /**
     * Intro static animations
     */
    FADE_IN("fade_in"),

    /**
     * Outro static animations
     */
    FADE_OUT("fade_out"),

    /**
     * Intro dynamic animations
     */
    SLIDE_FROM_LEFT("slide_from_left"),
    SLIDE_FROM_RIGHT("slide_from_right"),
    SLIDE_FROM_TOP("slide_from_top"),
    SLIDE_FROM_BOTTOM("slide_from_bottom"),

    /**
     * Outro dynamic animations
     */
    SLIDE_TO_LEFT("slide_to_left"),
    SLIDE_TO_RIGHT("slide_to_right"),
    SLIDE_TO_TOP("slide_to_top"),
    SLIDE_TO_BOTTOM("slide_to_bottom");


    companion object {
        fun fromValueOrNone(value: String) =
            values().firstOrNull { it.name.toLowerCase() == value } ?: NONE
    }
}