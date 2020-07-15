package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.OverlayObject

interface AnnotationListener {
    /**
     * add an overlay which:
     *
     * offset has not passed,
     * might or might not have animation
     *
     */
    fun onNewActionAvailable(actionEntity: ActionEntity)


    /**
     * add an overlay which:
     *
     * offset has passed,
     * either with no Intro animation,
     * or the animation time has passed too
     */
    fun onLingeringActionAvailable(actionEntity: ActionEntity)


    /**
     * add an overlay which:
     *
     * offset has passed,
     * has an Intro animation, and it's within the animation duration
     *
     */
    fun onLingeringIntroAnimationAvailable(
        actionEntity: ActionEntity,
        animationPosition: Long,
        isPlaying: Boolean
    )

    /**
     * add an overlay which:
     *
     * duration has passed,
     * has an Outro animation, and it's within the animation duration
     *
     */
    fun onLingeringOutroAnimationAvailableFromSeparateAction(
        relatedShowActionEntity: ActionEntity,
        hideActionEntity: ActionEntity,
        animationPosition: Long,
        isPlaying: Boolean
    )

    /**
     * add an overlay which:
     *
     * duration has passed,
     * has an Outro animation, and it's within the animation duration
     *
     */
    fun onLingeringOutroAnimationAvailableFromSameAction(
        relatedShowActionEntity: ActionEntity,
        animationPosition: Long,
        isPlaying: Boolean
    )

    /**
     * adds Outro animation to a currently displaying overlay
     *
     */
    fun onNewOutroAnimationAvailableSeparateAction(
        actionEntity: ActionEntity,
        hideActionEntity: ActionEntity
    )

    /**
     * runs Outro animation from a currently displaying overlay
     */
    fun onNewOutroAnimationAvailableSameCommand(
        actionEntity: ActionEntity
    )

//    fun clearScreen(customIdList: List<String>)

    // re-write
    // from now up to next second
    fun onNewOverlay(overlayObject: OverlayObject)
    fun onRemovalOverlay(overlayObject: OverlayObject)

    fun clearScreen(idList: List<String>)

    fun onLingeringIntroOverlay(
        overlayObject: OverlayObject,
        animationPosition: Long,
        isPlaying: Boolean
    )
    fun onLingeringOutroOverlay(
        overlayObject: OverlayObject,
        animationPosition: Long,
        isPlaying: Boolean
    )
    fun onLingeringOverlay(
        overlayObject: OverlayObject
    )


}
