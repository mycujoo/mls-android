package tv.mycujoo.domain.entity

enum class AnimationType {
    UNSPECIFIED,
    NONE,
    FADE_IN, FADE_OUT,
    SLIDE_FROM_LEFT, SLIDE_FROM_RIGHT,
    SLIDE_FROM_TOP, SLIDE_TO_TOP,
    SLIDE_FROM_BOTTOM, SLIDE_TO_BOTTOM,
    SLIDE_TO_LEFT, SLIDE_TO_RIGHT;

    companion object {
        fun fromValueOrNone(value: String) =
            values().firstOrNull { it.name.toLowerCase() == value } ?: NONE
    }
}