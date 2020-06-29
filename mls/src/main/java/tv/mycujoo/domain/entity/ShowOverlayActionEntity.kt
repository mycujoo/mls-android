package tv.mycujoo.domain.entity

data class ShowOverlayActionEntity(
    val id: String,
    val customId: String?,
    val svgUrl: String?,
    val size: Pair<Float, Float>,
    val duration: Long?
) {
}