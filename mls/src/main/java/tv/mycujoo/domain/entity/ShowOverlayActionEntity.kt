package tv.mycujoo.domain.entity

import java.io.InputStream

data class ShowOverlayActionEntity(
    val id: String,
    val customId: String?,
    val svgInputStream: InputStream?,
    val positionGuide: PositionGuide,
    val size: Pair<Float, Float>,
    val duration: Long?
) {
}