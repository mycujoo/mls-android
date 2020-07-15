package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.OverlayObject

interface AnnotationListener {

    /**
     * from now up to next second
     * add an overlay which:
     *
     * offset has not passed,
     * might or might not have intro animation
     *
     */
    fun onNewOverlay(overlayObject: OverlayObject)

    /**
     * removes an overlay which:
     * is already displayed,
     * might or might not have outro animation
     *
     */
    fun onRemovalOverlay(overlayObject: OverlayObject)

    /**
     * clears entire screen
     */
    fun clearScreen(idList: List<String>)


    /**
     * adds an overlay which:
     *
     * offset has passed,
     * has an Intro animation, and it's within the intro animation duration
     *
     */
    fun onLingeringIntroOverlay(
        overlayObject: OverlayObject,
        animationPosition: Long,
        isPlaying: Boolean
    )


    /**
     * adds an overlay which:
     *
     * duration has passed,
     * has an Outro animation, and it's within the outro animation duration
     *
     */
    fun onLingeringOutroOverlay(
        overlayObject: OverlayObject,
        animationPosition: Long,
        isPlaying: Boolean
    )


    /**
     * add an overlay which:
     *
     * offset has passed,
     * either with no Intro animation,
     * or the intro animation time has passed too
     *
     * either with no Outro animation, or the outro animation is not started yet
     */
    fun onLingeringOverlay(
        overlayObject: OverlayObject
    )


}
