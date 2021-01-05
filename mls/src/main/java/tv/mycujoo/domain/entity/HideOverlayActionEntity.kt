package tv.mycujoo.domain.entity

data class HideOverlayActionEntity(
    val id: String,
    val customId: String,
    val outroAnimationType: AnimationType,
    val outroAnimationDuration: Long
)