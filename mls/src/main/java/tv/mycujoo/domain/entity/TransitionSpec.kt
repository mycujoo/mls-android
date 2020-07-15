package tv.mycujoo.domain.entity

data class TransitionSpec(
    val offset: Long,
    val animationType: AnimationType,
    val animationDuration: Long
)