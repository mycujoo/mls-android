package tv.mycujoo.domain.entity

enum class AnimationType {
    NONE,
    FADE_IN, FADE_OUT,
    SLIDE_FROM_LEADING, SLIDE_FROM_TRAILING,
    SLIDE_TO_LEADING, SLIDE_TO_TRAILING;

    companion object {
        fun fromValueOrNone(value: String) = values().firstOrNull { it.name.toLowerCase() == value } ?: NONE
    }
}