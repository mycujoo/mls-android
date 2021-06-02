package tv.mycujoo.domain.entity

/**
 * Transition specification that Overlay view will have while bringing into screen, or moving out of screen.
 * @property offset the moment that transition should take place at
 * @property animationType type of animation that must be displayed
 * @property animationDuration length of animation (time is always in ms, unless mentioned otherwise)
 * @see AnimationType
 */
data class TransitionSpec(
    val offset: Long,
    val animationType: AnimationType,
    val animationDuration: Long
)