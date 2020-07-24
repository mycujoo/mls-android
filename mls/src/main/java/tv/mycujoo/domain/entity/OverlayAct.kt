package tv.mycujoo.domain.entity

enum class OverlayAct {
    DO_NOTHING,

    // regular play mode
    INTRO,
    OUTRO,

    // seek or jump play mode
    LINGERING_INTRO,
    LINGERING_MIDWAY,
    LINGERING_OUTRO;

}
