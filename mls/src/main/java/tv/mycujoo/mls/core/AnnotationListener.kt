package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.mls.entity.actions.ActionWrapper

interface AnnotationListener {
    fun onNewActionWrapperAvailable(actionWrapper: ActionWrapper)
    fun onNewRemovalWrapperAvailable(actionWrapper: ActionWrapper)


    /**
     * add an overlay which:
     *
     * offset has not passed,
     * might or might not have animation
     *
     */
    fun onNewActionAvailable(actionEntity: ActionEntity)


    /**
     * todo
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
    fun onLingeringOutroAnimationAvailable(
        relatedShowActionEntity: ActionEntity,
        hideActionEntity: ActionEntity,
        animationPosition: Long,
        isPlaying: Boolean
    )

    /**
     * Can not be used unless clear screen is revisited
     * update currently on-screen overlay
     */
    fun updateAnimations(
        actionEntity: ActionEntity,
        animationPosition: Long,
        isPlaying: Boolean
    )

    fun clearScreen(customIdList: List<String>)


}
