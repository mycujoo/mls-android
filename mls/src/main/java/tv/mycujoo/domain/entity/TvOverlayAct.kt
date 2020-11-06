package tv.mycujoo.domain.entity

enum class TvOverlayAct {
    DO_NOTHING,

    // regular play mode
    INTRO,
    OUTRO,
    REMOVE,

    // seek or jump play mode
    LINGERING_INTRO,
    LINGERING_OUTRO

}
