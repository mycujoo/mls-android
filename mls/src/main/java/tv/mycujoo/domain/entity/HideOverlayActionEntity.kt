package tv.mycujoo.domain.entity

data class HideOverlayActionEntity(
    val id: String,
    val customId: String?,
    val animationType: AnimationType,
    val animationDuration: Long
)